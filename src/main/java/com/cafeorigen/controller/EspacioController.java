package com.cafeorigen.controller;

import com.cafeorigen.model.Espacio;
import com.cafeorigen.model.Usuario;
import com.cafeorigen.repository.IEspacioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/espacios")
public class EspacioController {

    @Autowired
    private IEspacioRepository espacioRepository;

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
    public String listarEspacios(@RequestParam(value = "tipo", required = false) String tipo,
                                 HttpSession session,
                                 Model model) {
        if (!validarSesionYRol(session, "ADMINISTRADOR", "RECEPCIONISTA", "CLIENTE")) {
            return "redirect:/login";
        }

        List<Espacio> espacios;
        if (tipo != null && !tipo.isEmpty() && !"Todos".equals(tipo)) {
            espacios = espacioRepository.findByTipoAndEstado(tipo, "Disponible");
        } else {
            espacios = espacioRepository.findAll();
        }

        model.addAttribute("espacios", espacios);
        model.addAttribute("tipoSeleccionado", tipo);
        model.addAttribute("usuario", session.getAttribute("usuarioLogueado"));
        return "espacios/listado";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioRegistro(HttpSession session, Model model) {
        if (!validarSesionYRol(session, "ADMINISTRADOR", "RECEPCIONISTA")) {
            return "redirect:/login";
        }
        model.addAttribute("espacio", new Espacio());
        model.addAttribute("usuario", session.getAttribute("usuarioLogueado"));
        return "espacios/formulario";
    }

    @PostMapping("/guardar")
    public String guardarEspacio(@ModelAttribute("espacio") Espacio espacio, HttpSession session) {
        if (!validarSesionYRol(session, "ADMINISTRADOR", "RECEPCIONISTA")) {
            return "redirect:/login";
        }
        espacioRepository.save(espacio);
        return "redirect:/espacios";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable("id") Integer id, HttpSession session, Model model) {
        if (!validarSesionYRol(session, "ADMINISTRADOR", "RECEPCIONISTA")) {
            return "redirect:/login";
        }
        Espacio espacio = espacioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID de espacio inválido: " + id));
        model.addAttribute("espacio", espacio);
        model.addAttribute("usuario", session.getAttribute("usuarioLogueado"));
        return "espacios/formulario";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarEspacio(@PathVariable("id") Integer id, HttpSession session) {
        if (!validarSesionYRol(session, "ADMINISTRADOR")) {
            return "redirect:/login";
        }
        espacioRepository.deleteById(id);
        return "redirect:/espacios";
    }
}
