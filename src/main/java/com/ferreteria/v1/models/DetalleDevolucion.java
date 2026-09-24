package com.ferreteria.v1.models;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;

@Entity
@Table(name = "detalle_devolucion")
public class DetalleDevolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "devolucion_id", nullable = false)
    @JsonBackReference
    private Devolucion devolucion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioUnitario;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal;

    public Integer getId()                       { return id; }
    public void    setId(Integer id)             { this.id = id; }

    public Devolucion getDevolucion()                    { return devolucion; }
    public void       setDevolucion(Devolucion d)        { this.devolucion = d; }

    public Producto getProducto()                { return producto; }
    public void     setProducto(Producto p)      { this.producto = p; }

    public Integer  getCantidad()                { return cantidad; }
    public void     setCantidad(Integer c)       { this.cantidad = c; }

    public BigDecimal getPrecioUnitario()                { return precioUnitario; }
    public void       setPrecioUnitario(BigDecimal p)    { this.precioUnitario = p; }

    public BigDecimal getSubtotal()              { return subtotal; }
    public void       setSubtotal(BigDecimal s)  { this.subtotal = s; }
}
