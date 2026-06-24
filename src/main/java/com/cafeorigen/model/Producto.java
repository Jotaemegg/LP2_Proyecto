package com.cafeorigen.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "producto")
@Data
@NoArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Double precio;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(name = "id_categoria")
    private Integer idCategoria;

    @Column(name = "id_proveedor")
    private Integer idProveedor;

    @Column(nullable = false)
    private Integer estado = 1;

    @ManyToOne
    @JoinColumn(name = "id_categoria", insertable = false, updatable = false)
    private Categoria objCategoria;

    @ManyToOne
    @JoinColumn(name = "id_proveedor", insertable = false, updatable = false)
    private Proveedor objProveedor;
}
