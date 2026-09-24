package com.ferreteria.v1.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ventas")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "numero_comprobante", unique = true, length = 30)
    private String numeroComprobante;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(columnDefinition = "DECIMAL(10,2) DEFAULT 0")
    private Double subtotal = 0.0;

    @Column(columnDefinition = "DECIMAL(10,2) DEFAULT 0")
    private Double igv = 0.0;

    @Column(columnDefinition = "DECIMAL(10,2) DEFAULT 0")
    private Double total = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pago", length = 10)
    private TipoPago tipoPago = TipoPago.Efectivo;

    @Enumerated(EnumType.STRING)
    @Column(length = 12)
    private EstadoVenta estado = EstadoVenta.Completada;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comprobante", length = 10)
    private TipoComprobante tipoComprobante = TipoComprobante.Boleta;

    @Column(name = "ruc_cliente", length = 20)
    private String rucCliente;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TipoPago { Efectivo, Tarjeta, Yape, Plin }
    public enum EstadoVenta { Completada, Anulada }
    public enum TipoComprobante { Boleta, Factura }

    public TipoComprobante getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(TipoComprobante t) { this.tipoComprobante = t; }

    public String getRucCliente() { return rucCliente; }
    public void setRucCliente(String rucCliente) { this.rucCliente = rucCliente; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNumeroComprobante() { return numeroComprobante; }
    public void setNumeroComprobante(String numeroComprobante) { this.numeroComprobante = numeroComprobante; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

    public Double getIgv() { return igv; }
    public void setIgv(Double igv) { this.igv = igv; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public TipoPago getTipoPago() { return tipoPago; }
    public void setTipoPago(TipoPago tipoPago) { this.tipoPago = tipoPago; }

    public EstadoVenta getEstado() { return estado; }
    public void setEstado(EstadoVenta estado) { this.estado = estado; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}