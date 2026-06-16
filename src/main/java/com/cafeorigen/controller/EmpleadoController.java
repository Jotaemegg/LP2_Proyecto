package com.cafeorigen.controller;

import com.cafeorigen.model.Empleado;
import com.cafeorigen.model.Rol;
import com.cafeorigen.model.Usuario;
import com.cafeorigen.repository.IEmpleadoRepository;
import com.cafeorigen.repository.IRolRepository;
import com.cafeorigen.repository.IUsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/empleados")
public class EmpleadoController {

    @Autowired
    private IEmpleadoRepository empleadoRepository;

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private IRolRepository rolRepository;

    private boolean validarSesionYRol(HttpSession session, String... rolesValidos) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return false;
        
        String rolUsuario = usuario.getRol().getNombre();
        for (String rol : rolesValidos) {
            if (rol.equalsIgnoreCase(rolUsuario)) {
                return true;
            }
        }
        return false;
    }

    @GetMapping
    public String listarEmpleados(HttpSession session, Model model) {
        if (!validarSesionYRol(session, "ADMINISTRADOR")) {
            return "redirect:/dashboard";
        }
        
        List<Empleado> empleados = empleadoRepository.findAll();
        model.addAttribute("empleados", empleados);
        model.addAttribute("usuario", session.getAttribute("usuarioLogueado"));
        return "empleados/listado";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioRegistro(HttpSession session, Model model) {
        if (!validarSesionYRol(session, "ADMINISTRADOR")) {
            return "redirect:/dashboard";
        }
        
        model.addAttribute("empleado", new Empleado());
        // Solo mostrar roles de empleados (ADMINISTRADOR y RECEPCIONISTA), no CLIENTE
        List<Rol> rolesEmpleado = rolRepository.findAll()
                .stream()
                .filter(r -> !"CLIENTE".equalsIgnoreCase(r.getNombre()))
                .toList();
        model.addAttribute("roles", rolesEmpleado);
        model.addAttribute("usuario", session.getAttribute("usuarioLogueado"));
        return "empleados/formulario";
    }

    @PostMapping("/guardar")
    @Transactional
    public String guardarEmpleado(@ModelAttribute("empleado") Empleado empleado,
                                  @RequestParam("email") String email,
                                  @RequestParam("password") String password,
                                  @RequestParam("idRol") Integer idRol,
                                  HttpSession session,
                                  Model model) {
        if (!validarSesionYRol(session, "ADMINISTRADOR")) {
            return "redirect:/dashboard";
        }

        try {
            // Validar que no se asigne rol de CLIENTE a un empleado
            Rol rol = rolRepository.findById(idRol)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado."));
            if ("CLIENTE".equalsIgnoreCase(rol.getNombre())) {
                throw new RuntimeException("No se puede asignar el rol de Cliente a un empleado.");
            }

            Usuario usuario;
            if (empleado.getIdEmpleado() == null) {
                // Registro Nuevo
                if (usuarioRepository.findByEmail(email).isPresent()) {
                    throw new RuntimeException("El correo electrónico ya está en uso.");
                }
                usuario = new Usuario();
            } else {
                // Edición
                Empleado empDb = empleadoRepository.findById(empleado.getIdEmpleado())
                        .orElseThrow(() -> new RuntimeException("Empleado no encontrado."));
                usuario = empDb.getUsuario();
                // Validar si el correo cambió y está en uso
                if (!usuario.getEmail().equalsIgnoreCase(email) && usuarioRepository.findByEmail(email).isPresent()) {
                    throw new RuntimeException("El nuevo correo electrónico ya está en uso.");
                }
                // Preservar la fecha de contratación original
                empleado.setFechaContratacion(empDb.getFechaContratacion());
            }

            // Seteo del usuario
            usuario.setEmail(email);
            usuario.setPassword(password);
            usuario.setRol(rol);
            Usuario usuarioGuardado = usuarioRepository.save(usuario);

            // Seteo del empleado
            empleado.setUsuario(usuarioGuardado);
            if (empleado.getFechaContratacion() == null) {
                empleado.setFechaContratacion(LocalDate.now());
            }
            empleadoRepository.save(empleado);

            return "redirect:/empleados";
        } catch (Exception e) {
            model.addAttribute("error", "Error al guardar empleado: " + e.getMessage());
            model.addAttribute("empleado", empleado);
            // Solo mostrar roles de empleados, no CLIENTE
            List<Rol> rolesEmpleado = rolRepository.findAll()
                    .stream()
                    .filter(r -> !"CLIENTE".equalsIgnoreCase(r.getNombre()))
                    .toList();
            model.addAttribute("roles", rolesEmpleado);
            model.addAttribute("usuario", session.getAttribute("usuarioLogueado"));
            return "empleados/formulario";
        }
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable("id") Integer id, HttpSession session, Model model) {
        if (!validarSesionYRol(session, "ADMINISTRADOR")) {
            return "redirect:/dashboard";
        }

        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID de empleado inválido: " + id));
        model.addAttribute("empleado", empleado);
        // Solo mostrar roles de empleados, no CLIENTE
        List<Rol> rolesEmpleado = rolRepository.findAll()
                .stream()
                .filter(r -> !"CLIENTE".equalsIgnoreCase(r.getNombre()))
                .toList();
        model.addAttribute("roles", rolesEmpleado);
        model.addAttribute("usuario", session.getAttribute("usuarioLogueado"));
        return "empleados/formulario";
    }

    @GetMapping("/eliminar/{id}")
    @Transactional
    public String eliminarEmpleado(@PathVariable("id") Integer id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!validarSesionYRol(session, "ADMINISTRADOR")) {
            return "redirect:/dashboard";
        }

        Empleado empleado = empleadoRepository.findById(id).orElse(null);
        if (empleado != null) {
            // No permitir que un admin se elimine a sí mismo
            Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
            if (empleado.getUsuario().getIdUsuario().equals(usuarioLogueado.getIdUsuario())) {
                redirectAttributes.addFlashAttribute("error", "No puedes eliminarte a ti mismo del sistema.");
                return "redirect:/empleados";
            }
            // Borrar empleado y su usuario asociado (ON DELETE CASCADE en BD)
            empleadoRepository.delete(empleado);
            redirectAttributes.addFlashAttribute("msg", "Empleado eliminado correctamente.");
        }
        return "redirect:/empleados";
    }
}
