package com.cafeorigen.service;

import com.cafeorigen.model.*;
import com.cafeorigen.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReservaService {

    @Autowired
    private IReservaRepository reservaRepository;

    @Autowired
    private IReservaDetalleRepository reservaDetalleRepository;

    @Autowired
    private IConsumoDetalleRepository consumoDetalleRepository;

    @Autowired
    private IProductoRepository productoRepository;

    @Autowired
    private IEspacioRepository espacioRepository;

    @Autowired
    private IClienteRepository clienteRepository;

    /**
     * Registra una reserva de espacio y sus consumos de cafetería bajo una única transacción.
     * Si ocurre algún error o falta stock, se ejecuta rollback automático.
     */
    @Transactional(rollbackFor = Exception.class)
    public Reserva registrarReservaCompleta(Reserva reserva, ReservaDetalle detalleEspacio, List<ConsumoDetalle> consumos) throws Exception {
        
        // Validar que el cliente exista
        Cliente cliente = clienteRepository.findById(reserva.getCliente().getIdCliente())
                .orElseThrow(() -> new RuntimeException("El cliente seleccionado no existe en la base de datos."));
        reserva.setCliente(cliente);

        // 1. Guardar la cabecera de la reserva primero para generar el ID
        reserva.setTotalPago(0.0); // Se recalculará al final
        Reserva reservaGuardada = reservaRepository.save(reserva);
        
        double totalAcumulado = 0.0;

        // 2. Procesar y guardar el detalle del espacio alquilado
        Espacio espacio = espacioRepository.findById(detalleEspacio.getEspacio().getIdEspacio())
                .orElseThrow(() -> new RuntimeException("El espacio seleccionado no existe."));
        
        if ("Mantenimiento".equalsIgnoreCase(espacio.getEstado())) {
            throw new RuntimeException("El espacio '" + espacio.getNombre() + "' esta en mantenimiento.");
        }

        // Validar traslape de horarios (7:00 a 23:00)
        List<ReservaDetalle> reservasExistentes = reservaDetalleRepository.findByEspacioIdEspacioAndFechaReserva(espacio.getIdEspacio(), detalleEspacio.getFechaReserva());
        int selStart = detalleEspacio.getHoraInicio();
        int selEnd = selStart + detalleEspacio.getHorasUso();
        
        for (ReservaDetalle exist : reservasExistentes) {
            int exStart = exist.getHoraInicio();
            int exEnd = exStart + exist.getHorasUso();
            int exCleanEnd = exEnd + 1; // 1 hora de limpieza despues de la reserva
            
            if (selStart < exCleanEnd && selEnd > exStart) {
                throw new RuntimeException("El horario coincide con otra reserva o su hora de limpieza (Ocupado de " + exStart + ":00 a " + exEnd + ":00).");
            }
        }
        
        // Calcular subtotal de espacio
        double subtotalEspacio = espacio.getPrecioHora() * detalleEspacio.getHorasUso();
        detalleEspacio.setReserva(reservaGuardada);
        detalleEspacio.setEspacio(espacio);
        detalleEspacio.setSubtotal(subtotalEspacio);
        reservaDetalleRepository.save(detalleEspacio);
        
        totalAcumulado += subtotalEspacio;

        // 3. Procesar los consumos de cafetería asociados (si existen)
        if (consumos != null && !consumos.isEmpty()) {
            for (ConsumoDetalle consumo : consumos) {
                Producto producto = productoRepository.findById(consumo.getProducto().getIdProducto())
                        .orElseThrow(() -> new RuntimeException("El producto seleccionado no existe."));
                
                // Verificar si hay stock suficiente en la base de datos
                if (producto.getStock() < consumo.getCantidad()) {
                    throw new Exception("Stock insuficiente para el producto: " + producto.getNombre() + 
                            " (Stock actual: " + producto.getStock() + ", Solicitado: " + consumo.getCantidad() + ").");
                }
                
                // Descontar stock del producto
                producto.setStock(producto.getStock() - consumo.getCantidad());
                productoRepository.save(producto);
                
                // Calcular subtotal del consumo
                double subtotalConsumo = producto.getPrecio() * consumo.getCantidad();
                consumo.setReserva(reservaGuardada);
                consumo.setProducto(producto);
                consumo.setSubtotal(subtotalConsumo);
                consumoDetalleRepository.save(consumo);
                
                totalAcumulado += subtotalConsumo;
            }
        }

        // 4. Actualizar el total de la reserva cabecera
        reservaGuardada.setTotalPago(totalAcumulado);
        reservaRepository.save(reservaGuardada);

        return reservaGuardada;
    }
}
