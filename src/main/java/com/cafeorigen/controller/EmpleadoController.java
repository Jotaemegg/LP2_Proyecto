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

    private boolean esAdmin(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        return usuario != null && "ADMINISTRADOR".equalsIgnoreCase(usuario.getRol().getNombre());
    }

    private void cargarCombos(Model model, HttpSession session) {
        model.addAttribute("lstRoles", rolRepository.findAll());
        model.addAttribute("usuario", session.getAttribute("usuarioLogueado"));
    }

    @GetMapping
    public String cargar(@RequestParam(value = "buscar", required = false) String buscar,
                         HttpSession session, Model model) {
        if (!esAdmin(session)) return "redirect:/dashboard";

        List<Empleado> lista;
        if (buscar != null && !buscar.trim().isEmpty()) {
            lista = empleadoRepository.findByEstadoAndNombreContainingOrderByIdEmpleadoDesc(1, buscar);
        } else {
            lista = empleadoRepository.findByEstadoOrderByIdEmpleadoDesc(1);
        }

        model.addAttribute("empleado", new Empleado());
        model.addAttribute("lstEmpleados", lista);
        model.addAttribute("buscar", buscar);
        cargarCombos(model, session);
        return "empleados";
    }

    @PostMapping("/grabar")
    @Transactional
    public String grabar(@ModelAttribute("empleado") Empleado empleado,
                         @RequestParam("email") String email,
                         @RequestParam("password") String password,
                         @RequestParam("idRol") Integer idRol,
                         HttpSession session,
                         Model model) {
        if (!esAdmin(session)) return "redirect:/dashboard";

        try {
            Rol rol = rolRepository.findById(idRol)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado."));

            Usuario usuario;
            if (empleado.getIdEmpleado() == null) {
                if (usuarioRepository.findByEmail(email).isPresent()) {
                    throw new RuntimeException("El correo electrónico ya está en uso.");
                }
                usuario = new Usuario();
                empleado.setFechaContratacion(LocalDate.now());
                empleado.setEstado(1);
            } else {
                Empleado empDb = empleadoRepository.findById(empleado.getIdEmpleado())
                        .orElseThrow(() -> new RuntimeException("Empleado no encontrado."));
                usuario = empDb.getUsuario();
                if (!usuario.getEmail().equalsIgnoreCase(email) && usuarioRepository.findByEmail(email).isPresent()) {
                    throw new RuntimeException("El nuevo correo electrónico ya está en uso.");
                }
                empleado.setFechaContratacion(empDb.getFechaContratacion());
                empleado.setEstado(empDb.getEstado());
            }

            usuario.setEmail(email);
            usuario.setPassword(password);
            usuario.setRol(rol);
            if (usuario.getEstado() == null) {
                usuario.setEstado(1);
            }

            empleado.setUsuario(usuario);
            empleadoRepository.save(empleado);

            model.addAttribute("mensaje", "Empleado guardado correctamente.");
            model.addAttribute("cssmensaje", "alert alert-success");
            model.addAttribute("empleado", new Empleado());
        } catch (Exception e) {
            model.addAttribute("mensaje", "Error al guardar: " + e.getMessage());
            model.addAttribute("cssmensaje", "alert alert-danger");
            model.addAttribute("empleado", empleado);
        }

        model.addAttribute("lstEmpleados", empleadoRepository.findByEstadoOrderByIdEmpleadoDesc(1));
        cargarCombos(model, session);
        return "empleados";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Integer id, HttpSession session, Model model) {
        if (!esAdmin(session)) return "redirect:/dashboard";

        Empleado empleado = empleadoRepository.findById(id).orElse(new Empleado());
        model.addAttribute("empleado", empleado);
        model.addAttribute("lstEmpleados", empleadoRepository.findByEstadoOrderByIdEmpleadoDesc(1));
        cargarCombos(model, session);
        return "empleados";
    }

    @GetMapping("/eliminar/{id}")
    @Transactional
    public String eliminar(@PathVariable("id") Integer id, HttpSession session) {
        if (!esAdmin(session)) return "redirect:/dashboard";

        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        empleadoRepository.findById(id).ifPresent(emp -> {
            if (!emp.getUsuario().getIdUsuario().equals(usuarioLogueado.getIdUsuario())) {
                emp.setEstado(0);
                emp.getUsuario().setEstado(0);
                empleadoRepository.save(emp);
            }
        });
        return "redirect:/empleados";
    }
}
