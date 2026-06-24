package com.cafeorigen.controller;

import com.cafeorigen.model.Producto;
import com.cafeorigen.repository.ICategoriaRepository;
import com.cafeorigen.repository.IProductoRepository;
import com.cafeorigen.repository.IProveedorRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private IProductoRepository productoRepository;

    @Autowired
    private ICategoriaRepository categoriaRepository;

    @Autowired
    private IProveedorRepository proveedorRepository;

    private boolean sesionActiva(HttpSession session) {
        return session.getAttribute("usuarioLogueado") != null;
    }

    private void cargarDatos(Model model, HttpSession session) {
        model.addAttribute("lstProductos", productoRepository.findByEstadoOrderByIdProductoDesc(1));
        model.addAttribute("lstCategorias", categoriaRepository.findAll());
        model.addAttribute("lstProveedores", proveedorRepository.findAll());
        model.addAttribute("usuario", session.getAttribute("usuarioLogueado"));
    }

    @GetMapping
    public String cargar(HttpSession session, Model model) {
        if (!sesionActiva(session)) return "redirect:/login";

        model.addAttribute("producto", new Producto());
        cargarDatos(model, session);
        return "productos";
    }

    @PostMapping("/grabar")
    public String grabar(@ModelAttribute("producto") Producto producto, HttpSession session, Model model) {
        if (!sesionActiva(session)) return "redirect:/login";

        try {
            if (producto.getEstado() == null) {
                producto.setEstado(1);
            }
            productoRepository.save(producto);
            model.addAttribute("mensaje", "Producto guardado correctamente.");
            model.addAttribute("cssmensaje", "alert alert-success");
        } catch (Exception e) {
            model.addAttribute("mensaje", "Error al guardar el producto.");
            model.addAttribute("cssmensaje", "alert alert-danger");
        }

        model.addAttribute("producto", new Producto());
        cargarDatos(model, session);
        return "productos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Integer id, HttpSession session, Model model) {
        if (!sesionActiva(session)) return "redirect:/login";

        Producto producto = productoRepository.findById(id).orElse(new Producto());
        model.addAttribute("producto", producto);
        cargarDatos(model, session);
        return "productos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Integer id, HttpSession session) {
        if (!sesionActiva(session)) return "redirect:/login";

        productoRepository.findById(id).ifPresent(p -> {
            p.setEstado(0);
            productoRepository.save(p);
        });
        return "redirect:/productos";
    }
}
