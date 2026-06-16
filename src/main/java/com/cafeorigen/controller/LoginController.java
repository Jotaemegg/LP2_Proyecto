package com.cafeorigen.controller;

import com.cafeorigen.model.Cliente;
import com.cafeorigen.model.Empleado;
import com.cafeorigen.model.Usuario;
import com.cafeorigen.repository.IClienteRepository;
import com.cafeorigen.repository.IEmpleadoRepository;
import com.cafeorigen.repository.IUsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private IClienteRepository clienteRepository;

    @Autowired
    private IEmpleadoRepository empleadoRepository;

    @GetMapping({"/", "/login"})
    public String mostrarLogin(HttpSession session) {
        if (session.getAttribute("usuarioLogueado") != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam("email") String email,
                               @RequestParam("password") String password,
                               HttpSession session,
                               Model model) {
        
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailAndPassword(email, password);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            String rolNombre = usuario.getRol().getNombre();
            String nombreMostrado = usuario.getEmail();
            
            // Cargar y guardar en sesión el perfil correspondiente
            if ("CLIENTE".equalsIgnoreCase(rolNombre)) {
                Optional<Cliente> clienteOpt = clienteRepository.findByUsuarioIdUsuario(usuario.getIdUsuario());
                if (clienteOpt.isPresent()) {
                    Cliente cliente = clienteOpt.get();
                    session.setAttribute("clienteLogueado", cliente);
                    nombreMostrado = cliente.getNombre();
                }
            } else if ("ADMINISTRADOR".equalsIgnoreCase(rolNombre) || "RECEPCIONISTA".equalsIgnoreCase(rolNombre)) {
                Optional<Empleado> empleadoOpt = empleadoRepository.findByUsuarioIdUsuario(usuario.getIdUsuario());
                if (empleadoOpt.isPresent()) {
                    Empleado empleado = empleadoOpt.get();
                    session.setAttribute("empleadoLogueado", empleado);
                    nombreMostrado = empleado.getNombre();
                }
            }
            
            usuario.setNombre(nombreMostrado);
            session.setAttribute("usuarioLogueado", usuario);

            return "redirect:/dashboard";
        } else {
            model.addAttribute("error", "Correo o contraseña incorrectos.");
            return "login";
        }
    }

    @GetMapping("/dashboard")
    public String mostrarDashboard(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        // Obtener el nombre del perfil correspondiente
        String nombreMostrado = usuario.getEmail();
        if ("CLIENTE".equalsIgnoreCase(usuario.getRol().getNombre())) {
            Cliente cliente = (Cliente) session.getAttribute("clienteLogueado");
            if (cliente != null) nombreMostrado = cliente.getNombre();
        } else {
            Empleado empleado = (Empleado) session.getAttribute("empleadoLogueado");
            if (empleado != null) nombreMostrado = empleado.getNombre();
        }

        usuario.setNombre(nombreMostrado);
        model.addAttribute("usuario", usuario);
        model.addAttribute("nombreMostrado", nombreMostrado);
        return "dashboard";
    }

    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
