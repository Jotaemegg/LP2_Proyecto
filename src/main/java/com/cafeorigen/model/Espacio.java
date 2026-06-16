package com.cafeorigen.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "espacio")
@Data
@NoArgsConstructor
public class Espacio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_espacio")
    private Integer idEspacio;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String tipo; // 'Escritorio', 'Sala de Reunión', 'Oficina'

    @Column(name = "precio_hora", nullable = false)
    private Double precioHora;

    @Column(nullable = false)
    private String estado = "Disponible"; // 'Disponible', 'Ocupado', 'Mantenimiento'
}
