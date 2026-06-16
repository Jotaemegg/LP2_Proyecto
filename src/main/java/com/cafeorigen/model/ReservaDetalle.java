package com.cafeorigen.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "reserva_detalle")
@Data
@NoArgsConstructor
public class ReservaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva_detalle")
    private Integer idReservaDetalle;

    @ManyToOne
    @JoinColumn(name = "id_reserva", nullable = false)
    private Reserva reserva;

    @ManyToOne
    @JoinColumn(name = "id_espacio", nullable = false)
    private Espacio espacio;

    @Column(name = "fecha_reserva", nullable = false)
    private LocalDate fechaReserva;

    @Column(name = "hora_inicio", nullable = false)
    private Integer horaInicio = 7;

    @Column(name = "horas_uso", nullable = false)
    private Integer horasUso;

    @Column(nullable = false)
    private Double subtotal;
}
