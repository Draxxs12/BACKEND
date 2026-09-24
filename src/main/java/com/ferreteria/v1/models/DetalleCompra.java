package com.ferreteria.v1.models;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "detalle_compra")
public class DetalleCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "compra_id", nullable = false)
    @JsonBackReference
    private Compra compra;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioUnitario;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal;

    public Integer getId()                              { return id; }
    public void    setId(Integer id)                    { this.id = id; }

    public Compra  getCompra()                          { return compra; }
    public void    setCompra(Compra compra)              { this.compra = compra; }

    public Producto getProducto()                       { return producto; }
    public void     setProducto(Producto producto)      { this.producto = producto; }

    public Integer  getCantidad()                       { return cantidad; }
    public void     setCantidad(Integer cantidad)       { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario()                              { return precioUnitario; }
    public void       setPrecioUnitario(BigDecimal precioUnitario)     { this.precioUnitario = precioUnitario; }

    public BigDecimal getSubtotal()                         { return subtotal; }
    public void       setSubtotal(BigDecimal subtotal)      { this.subtotal = subtotal; }
}