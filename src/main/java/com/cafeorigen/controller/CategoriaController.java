package com.cafeorigen.controller;

import com.cafeorigen.model.Categoria;
import com.cafeorigen.repository.ICategoriaRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    @Autowired
    private ICategoriaRepository categoriaRepository;

    private boolean sesionActiva(HttpSession session) {
        return session.getAttribute("usuarioLogueado") != null;
    }

    private void cargarDatos(Model model, HttpSession session) {
        model.addAttribute("lstCategorias", categoriaRepository.findByEstadoOrderByIdCategoriaDesc(1));
        model.addAttribute("usuario", session.getAttribute("usuarioLogueado"));
    }

    @GetMapping
    public String cargar(HttpSession session, Model model) {
        if (!sesionActiva(session)) return "redirect:/login";

        model.addAttribute("categoria", new Categoria());
        cargarDatos(model, session);
        return "categorias";
    }

    @PostMapping("/grabar")
    public String grabar(@ModelAttribute("categoria") Categoria categoria, HttpSession session, Model model) {
        if (!sesionActiva(session)) return "redirect:/login";

        try {
            if (categoria.getEstado() == null) {
                categoria.setEstado(1);
            }
            categoriaRepository.save(categoria);
            model.addAttribute("mensaje", "Categoría guardada correctamente.");
            model.addAttribute("cssmensaje", "alert alert-success");
        } catch (Exception e) {
            model.addAttribute("mensaje", "Error al guardar la categoría.");
            model.addAttribute("cssmensaje", "alert alert-danger");
        }

        model.addAttribute("categoria", new Categoria());
        cargarDatos(model, session);
        return "categorias";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Integer id, HttpSession session, Model model) {
        if (!sesionActiva(session)) return "redirect:/login";

        Categoria categoria = categoriaRepository.findById(id).orElse(new Categoria());
        model.addAttribute("categoria", categoria);
        cargarDatos(model, session);
        return "categorias";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Integer id, HttpSession session) {
        if (!sesionActiva(session)) return "redirect:/login";

        categoriaRepository.findById(id).ifPresent(c -> {
            c.setEstado(0);
            categoriaRepository.save(c);
        });
        return "redirect:/categorias";
    }
}
