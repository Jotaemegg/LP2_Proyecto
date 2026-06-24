package com.cafeorigen.controller;

import com.cafeorigen.model.Proveedor;
import com.cafeorigen.repository.IProveedorRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/proveedores")
public class ProveedorController {

    @Autowired
    private IProveedorRepository proveedorRepository;

    private boolean sesionActiva(HttpSession session) {
        return session.getAttribute("usuarioLogueado") != null;
    }

    private void cargarDatos(Model model, HttpSession session) {
        model.addAttribute("lstProveedores", proveedorRepository.findByEstadoOrderByIdProveedorDesc(1));
        model.addAttribute("usuario", session.getAttribute("usuarioLogueado"));
    }

    @GetMapping
    public String cargar(HttpSession session, Model model) {
        if (!sesionActiva(session)) return "redirect:/login";

        model.addAttribute("proveedor", new Proveedor());
        cargarDatos(model, session);
        return "proveedores";
    }

    @PostMapping("/grabar")
    public String grabar(@ModelAttribute("proveedor") Proveedor proveedor, HttpSession session, Model model) {
        if (!sesionActiva(session)) return "redirect:/login";

        try {
            if (proveedor.getEstado() == null) {
                proveedor.setEstado(1);
            }
            proveedorRepository.save(proveedor);
            model.addAttribute("mensaje", "Proveedor guardado correctamente.");
            model.addAttribute("cssmensaje", "alert alert-success");
        } catch (Exception e) {
            model.addAttribute("mensaje", "Error al guardar el proveedor.");
            model.addAttribute("cssmensaje", "alert alert-danger");
        }

        model.addAttribute("proveedor", new Proveedor());
        cargarDatos(model, session);
        return "proveedores";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Integer id, HttpSession session, Model model) {
        if (!sesionActiva(session)) return "redirect:/login";

        Proveedor proveedor = proveedorRepository.findById(id).orElse(new Proveedor());
        model.addAttribute("proveedor", proveedor);
        cargarDatos(model, session);
        return "proveedores";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Integer id, HttpSession session) {
        if (!sesionActiva(session)) return "redirect:/login";

        proveedorRepository.findById(id).ifPresent(p -> {
            p.setEstado(0);
            proveedorRepository.save(p);
        });
        return "redirect:/proveedores";
    }
}
