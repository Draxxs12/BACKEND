package com.ferreteria.v1.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "compras")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "numero_orden", unique = true, length = 30)
    private String numeroOrden;

    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal igv = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pago")
    private TipoPago tipoPago = TipoPago.Efectivo;

    @Enumerated(EnumType.STRING)
    private Estado estado = Estado.Pendiente;

    @Column(name = "fecha_esperada")
    private LocalDate fechaEsperada;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<DetalleCompra> detalles = new ArrayList<>();

    public enum TipoPago { Efectivo, Transferencia, Credito }
    public enum Estado   { Pendiente, Recibida, Anulada }

    public Integer       getId()                              { return id; }
    public void          setId(Integer id)                    { this.id = id; }

    public String        getNumeroOrden()                     { return numeroOrden; }
    public void          setNumeroOrden(String numeroOrden)   { this.numeroOrden = numeroOrden; }

    public Proveedor     getProveedor()                       { return proveedor; }
    public void          setProveedor(Proveedor proveedor)    { this.proveedor = proveedor; }

    public Usuario       getUsuario()                         { return usuario; }
    public void          setUsuario(Usuario usuario)          { this.usuario = usuario; }

    public BigDecimal    getSubtotal()                        { return subtotal; }
    public void          setSubtotal(BigDecimal subtotal)     { this.subtotal = subtotal; }

    public BigDecimal    getIgv()                             { return igv; }
    public void          setIgv(BigDecimal igv)               { this.igv = igv; }

    public BigDecimal    getTotal()                           { return total; }
    public void          setTotal(BigDecimal total)           { this.total = total; }

    public TipoPago      getTipoPago()                        { return tipoPago; }
    public void          setTipoPago(TipoPago tipoPago)       { this.tipoPago = tipoPago; }

    public Estado        getEstado()                          { return estado; }
    public void          setEstado(Estado estado)             { this.estado = estado; }

    public LocalDate     getFechaEsperada()                           { return fechaEsperada; }
    public void          setFechaEsperada(LocalDate fechaEsperada)    { this.fechaEsperada = fechaEsperada; }

    public String        getObservaciones()                           { return observaciones; }
    public void          setObservaciones(String observaciones)       { this.observaciones = observaciones; }

    public LocalDateTime getCreatedAt()                               { return createdAt; }
    public void          setCreatedAt(LocalDateTime createdAt)        { this.createdAt = createdAt; }

    public List<DetalleCompra> getDetalles()                              { return detalles; }
    public void                setDetalles(List<DetalleCompra> detalles)  { this.detalles = detalles; }
}