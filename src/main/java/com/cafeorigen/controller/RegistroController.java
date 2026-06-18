package com.cafeorigen.controller;

import com.cafeorigen.model.Cliente;
import com.cafeorigen.model.Rol;
import com.cafeorigen.model.Usuario;
import com.cafeorigen.repository.IClienteRepository;
import com.cafeorigen.repository.IRolRepository;
import com.cafeorigen.repository.IUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class RegistroController {

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private IClienteRepository clienteRepository;

    @Autowired
    private IRolRepository rolRepository;

    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "registro";
    }

    private void prellenarFormulario(Model model, String nombre, String email, String telefono, String direccion, String dni) {
        model.addAttribute("nombre", nombre);
        model.addAttribute("email", email);
        model.addAttribute("telefono", telefono);
        model.addAttribute("direccion", direccion);
        model.addAttribute("dni", dni);
    }

    @PostMapping("/registro")
    @Transactional
    public String procesarRegistro(@RequestParam("nombre") String nombre,
                                   @RequestParam("email") String email,
                                   @RequestParam("password") String password,
                                   @RequestParam("telefono") String telefono,
                                   @RequestParam("direccion") String direccion,
                                   @RequestParam("dni") String dni,
                                   Model model) {

        if (usuarioRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "El correo electrónico ya está registrado.");
            prellenarFormulario(model, nombre, email, telefono, direccion, dni);
            return "registro";
        }

        if (clienteRepository.findByDni(dni).isPresent()) {
            model.addAttribute("error", "El DNI ingresado ya está registrado.");
            prellenarFormulario(model, nombre, email, telefono, direccion, dni);
            return "registro";
        }

        try {
            Rol rolCliente = rolRepository.findById(3)
                    .orElseThrow(() -> new RuntimeException("Rol de Cliente no encontrado."));

            Usuario usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setPassword(password);
            usuario.setRol(rolCliente);
            Usuario usuarioGuardado = usuarioRepository.save(usuario);

            Cliente cliente = new Cliente();
            cliente.setUsuario(usuarioGuardado);
            cliente.setNombre(nombre);
            cliente.setTelefono(telefono);
            cliente.setDireccion(direccion);
            cliente.setDni(dni);
            clienteRepository.save(cliente);

            model.addAttribute("msg", "Cuenta creada exitosamente. Ya puedes iniciar sesión.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "Error al crear la cuenta: " + e.getMessage());
            prellenarFormulario(model, nombre, email, telefono, direccion, dni);
            return "registro";
        }
    }
}
