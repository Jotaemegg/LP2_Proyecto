package com.cafeorigen.controller;

import com.cafeorigen.model.*;
import com.cafeorigen.repository.*;
import com.cafeorigen.service.ReservaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/reservas")
public class ReservaController {

    @Autowired
    private IReservaRepository reservaRepository;

    @Autowired
    private IReservaDetalleRepository reservaDetalleRepository;

    @Autowired
    private IConsumoDetalleRepository consumoDetalleRepository;

    @Autowired
    private IEspacioRepository espacioRepository;

    @Autowired
    private IProductoRepository productoRepository;

    @Autowired
    private IClienteRepository clienteRepository;

    @Autowired
    private ReservaService reservaService;

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
    public String listarReservas(HttpSession session, Model model) {
        if (!validarSesionYRol(session, "ADMINISTRADOR", "RECEPCIONISTA", "CLIENTE")) {
            return "redirect:/login";
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        List<Reserva> reservas;
        
        if ("CLIENTE".equalsIgnoreCase(usuario.getRol().getNombre())) {
            Cliente cliente = (Cliente) session.getAttribute("clienteLogueado");
            reservas = reservaRepository.findByClienteIdCliente(cliente.getIdCliente());
        } else {
            reservas = reservaRepository.findAll();
        }

        model.addAttribute("reservas", reservas);
        model.addAttribute("usuario", usuario);
        return "reservas/listado";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioRegistro(HttpSession session, Model model) {
        if (!validarSesionYRol(session, "ADMINISTRADOR", "RECEPCIONISTA", "CLIENTE")) {
            return "redirect:/login";
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        // Mostrar todos los espacios que no estén en mantenimiento
        List<Espacio> espaciosDisponibles = espacioRepository.findByEstadoNot("Mantenimiento");
        List<Producto> productosConStock = productoRepository.findByStockGreaterThan(0);

        if (!"CLIENTE".equalsIgnoreCase(usuario.getRol().getNombre())) {
            model.addAttribute("clientes", clienteRepository.findAll());
        }

        model.addAttribute("espacios", espaciosDisponibles);
        model.addAttribute("productos", productosConStock);
        model.addAttribute("usuario", usuario);
        return "reservas/formulario";
    }

    @PostMapping("/guardar")
    public String guardarReserva(@RequestParam("idEspacio") Integer idEspacio,
                                 @RequestParam("fechaReserva") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaReserva,
                                 @RequestParam("horaInicio") Integer horaInicio,
                                 @RequestParam("horasUso") Integer horasUso,
                                 @RequestParam(value = "idCliente", required = false) Integer idCliente,
                                 @RequestParam(value = "productos", required = false) List<Integer> productosIds,
                                 @RequestParam(value = "cantidades", required = false) List<Integer> cantidades,
                                 HttpSession session,
                                 Model model) {

        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado == null) {
            return "redirect:/login";
        }

        Reserva reserva = new Reserva();
        Cliente clienteAsociado;

        // Determinar qué cliente se asocia a la reserva
        if ("CLIENTE".equalsIgnoreCase(usuarioLogueado.getRol().getNombre())) {
            clienteAsociado = (Cliente) session.getAttribute("clienteLogueado");
        } else {
            // El empleado/admin seleccionó un cliente
            if (idCliente == null) {
                model.addAttribute("error", "Debe seleccionar un cliente para la reserva.");
                cargarFormularioError(model, usuarioLogueado);
                return "reservas/formulario";
            }
            clienteAsociado = clienteRepository.findById(idCliente).orElse(null);
        }
        reserva.setCliente(clienteAsociado);

        ReservaDetalle detalleEspacio = new ReservaDetalle();
        Espacio espacio = new Espacio();
        espacio.setIdEspacio(idEspacio);
        detalleEspacio.setEspacio(espacio);
        detalleEspacio.setFechaReserva(fechaReserva);
        detalleEspacio.setHoraInicio(horaInicio);
        detalleEspacio.setHorasUso(horasUso);

        List<ConsumoDetalle> consumos = new ArrayList<>();
        if (productosIds != null && cantidades != null) {
            for (int i = 0; i < productosIds.size(); i++) {
                Integer prodId = productosIds.get(i);
                Integer cant = cantidades.get(i);
                if (cant != null && cant > 0) {
                    ConsumoDetalle consumo = new ConsumoDetalle();
                    Producto prod = new Producto();
                    prod.setIdProducto(prodId);
                    consumo.setProducto(prod);
                    consumo.setCantidad(cant);
                    consumos.add(consumo);
                }
            }
        }

        try {
            reservaService.registrarReservaCompleta(reserva, detalleEspacio, consumos);
            return "redirect:/reservas";
        } catch (Exception e) {
            model.addAttribute("error", "Fallo al registrar la reserva: " + e.getMessage());
            cargarFormularioError(model, usuarioLogueado);
            return "reservas/formulario";
        }
    }

    private void cargarFormularioError(Model model, Usuario usuarioLogueado) {
        model.addAttribute("espacios", espacioRepository.findByEstadoNot("Mantenimiento"));
        model.addAttribute("productos", productoRepository.findByStockGreaterThan(0));
        if (!"CLIENTE".equalsIgnoreCase(usuarioLogueado.getRol().getNombre())) {
            model.addAttribute("clientes", clienteRepository.findAll());
        }
        model.addAttribute("usuario", usuarioLogueado);
    }

    @GetMapping("/ver/{id}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String verDetallesReserva(@PathVariable("id") Integer id,
                                     @RequestParam(value = "error", required = false) String error,
                                     HttpSession session,
                                     Model model) {
        if (!validarSesionYRol(session, "ADMINISTRADOR", "RECEPCIONISTA", "CLIENTE")) {
            return "redirect:/login";
        }

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID de reserva inválido: " + id));

        if ("CLIENTE".equalsIgnoreCase(usuario.getRol().getNombre())) {
            Cliente cliente = (Cliente) session.getAttribute("clienteLogueado");
            if (cliente == null || reserva.getCliente() == null
                    || !reserva.getCliente().getIdCliente().equals(cliente.getIdCliente())) {
                return "redirect:/reservas";
            }
        }
        List<ReservaDetalle> detallesEspacio = reservaDetalleRepository.findByReservaIdReserva(id);
        List<ConsumoDetalle> consumos = consumoDetalleRepository.findByReservaIdReserva(id);

        if ("pdf".equalsIgnoreCase(error)) {
            model.addAttribute("error", "No se pudo generar el PDF de la boleta. Verifica que MySQL esté activo e intenta nuevamente.");
        }

        model.addAttribute("reserva", reserva);
        model.addAttribute("detallesEspacio", detallesEspacio);
        model.addAttribute("consumos", consumos);
        model.addAttribute("usuario", session.getAttribute("usuarioLogueado"));
        return "reservas/detalle";
    }

    @GetMapping("/ocupadas")
    @ResponseBody
    public List<Map<String, Object>> obtenerHorasOcupadas(@RequestParam("idEspacio") Integer idEspacio,
                                                          @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        List<ReservaDetalle> detalles = reservaDetalleRepository.findByEspacioIdEspacioAndFechaReserva(idEspacio, fecha);
        List<Map<String, Object>> list = new ArrayList<>();
        for (ReservaDetalle d : detalles) {
            Map<String, Object> map = new HashMap<>();
            map.put("horaInicio", d.getHoraInicio());
            map.put("horasUso", d.getHorasUso());
            list.add(map);
        }
        return list;
    }

    @GetMapping("/eliminar/{id}")
    @org.springframework.transaction.annotation.Transactional
    public String eliminarReserva(@PathVariable("id") Integer id, HttpSession session) {
        if (!validarSesionYRol(session, "ADMINISTRADOR", "RECEPCIONISTA")) {
            return "redirect:/login";
        }
        Reserva res = reservaRepository.findById(id).orElse(null);
        if (res != null) {
            // Restaurar stock de productos
            List<ConsumoDetalle> consumos = consumoDetalleRepository.findByReservaIdReserva(id);
            for (ConsumoDetalle cd : consumos) {
                Producto prod = cd.getProducto();
                if (prod != null) {
                    prod.setStock(prod.getStock() + cd.getCantidad());
                    productoRepository.save(prod);
                }
            }
            reservaRepository.delete(res);
        }
        return "redirect:/reservas";
    }
}
