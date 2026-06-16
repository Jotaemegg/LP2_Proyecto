package com.cafeorigen.controller;

import com.cafeorigen.model.Producto;
import com.cafeorigen.model.Usuario;
import com.cafeorigen.repository.IProductoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private IProductoRepository productoRepository;

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
    public String listarProductos(@RequestParam(value = "categoria", required = false) String categoria,
                                  HttpSession session,
                                  Model model) {
        if (!validarSesionYRol(session, "ADMINISTRADOR", "RECEPCIONISTA", "CLIENTE")) {
            return "redirect:/login";
        }

        List<Producto> productos;
        if (categoria != null && !categoria.isEmpty() && !"Todas".equals(categoria)) {
            productos = productoRepository.findByCategoria(categoria);
        } else {
            productos = productoRepository.findAll();
        }

        model.addAttribute("productos", productos);
        model.addAttribute("categoriaSeleccionada", categoria);
        model.addAttribute("usuario", session.getAttribute("usuarioLogueado"));
        return "productos/listado";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioRegistro(HttpSession session, Model model) {
        if (!validarSesionYRol(session, "ADMINISTRADOR")) {
            return "redirect:/login";
        }
        model.addAttribute("producto", new Producto());
        model.addAttribute("usuario", session.getAttribute("usuarioLogueado"));
        return "productos/formulario";
    }

    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute("producto") Producto producto, HttpSession session) {
        if (!validarSesionYRol(session, "ADMINISTRADOR")) {
            return "redirect:/login";
        }
        productoRepository.save(producto);
        return "redirect:/productos";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable("id") Integer id, HttpSession session, Model model) {
        if (!validarSesionYRol(session, "ADMINISTRADOR")) {
            return "redirect:/login";
        }
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID de producto inválido: " + id));
        model.addAttribute("producto", producto);
        model.addAttribute("usuario", session.getAttribute("usuarioLogueado"));
        return "productos/formulario";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable("id") Integer id, HttpSession session) {
        if (!validarSesionYRol(session, "ADMINISTRADOR")) {
            return "redirect:/login";
        }
        productoRepository.deleteById(id);
        return "redirect:/productos";
    }
}
