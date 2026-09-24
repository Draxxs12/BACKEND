package com.ferreteria.v1.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;

@Entity
@Table(name = "devoluciones")
public class Devolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "numero_nota", unique = true, length = 30)
    private String numeroNota;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(length = 255)
    private String motivo;

    @Column(name = "monto_reembolso", precision = 10, scale = 2, nullable = false)
    private BigDecimal montoReembolso = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_reembolso", length = 12)
    private TipoReembolso tipoReembolso = TipoReembolso.Efectivo;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "devolucion", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<DetalleDevolucion> detalles = new ArrayList<>();

    public enum TipoReembolso { Efectivo, Tarjeta, NotaCredito }

    public Integer getId()                        { return id; }
    public void    setId(Integer id)              { this.id = id; }

    public String  getNumeroNota()                { return numeroNota; }
    public void    setNumeroNota(String n)        { this.numeroNota = n; }

    public Venta   getVenta()                     { return venta; }
    public void    setVenta(Venta venta)          { this.venta = venta; }

    public Usuario getUsuario()                   { return usuario; }
    public void    setUsuario(Usuario usuario)    { this.usuario = usuario; }

    public String  getMotivo()                    { return motivo; }
    public void    setMotivo(String motivo)       { this.motivo = motivo; }

    public BigDecimal getMontoReembolso()                 { return montoReembolso; }
    public void       setMontoReembolso(BigDecimal m)     { this.montoReembolso = m; }

    public TipoReembolso getTipoReembolso()                  { return tipoReembolso; }
    public void          setTipoReembolso(TipoReembolso t)   { this.tipoReembolso = t; }

    public LocalDateTime getCreatedAt()                  { return createdAt; }
    public void          setCreatedAt(LocalDateTime c)   { this.createdAt = c; }

    public List<DetalleDevolucion> getDetalles()                       { return detalles; }
    public void setDetalles(List<DetalleDevolucion> detalles)          { this.detalles = detalles; }
}
