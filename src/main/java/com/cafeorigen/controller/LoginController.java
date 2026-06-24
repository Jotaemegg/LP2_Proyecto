package com.cafeorigen.controller;

import com.cafeorigen.model.Empleado;
import com.cafeorigen.model.Usuario;
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

        if (usuarioOpt.isPresent() && usuarioOpt.get().getEstado() == 1) {
            Usuario usuario = usuarioOpt.get();

            String nombreMostrado = usuario.getEmail();
            Optional<Empleado> empleadoOpt = empleadoRepository.findByUsuarioIdUsuario(usuario.getIdUsuario());
            if (empleadoOpt.isPresent()) {
                nombreMostrado = empleadoOpt.get().getNombre();
            }

            usuario.setNombre(nombreMostrado);
            session.setAttribute("usuarioLogueado", usuario);
            return "redirect:/dashboard";
        }

        model.addAttribute("error", "Correo o contraseña incorrectos.");
        return "login";
    }

    @GetMapping("/dashboard")
    public String mostrarDashboard(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("usuario", usuario);
        return "dashboard";
    }

    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
