package com.ferreteria.v1.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "caja")
public class Caja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "monto_inicial", precision = 10, scale = 2, nullable = false)
    private BigDecimal montoInicial = BigDecimal.ZERO;

    @Column(name = "monto_final", precision = 10, scale = 2)
    private BigDecimal montoFinal;

    @Column(name = "total_ventas", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalVentas = BigDecimal.ZERO;

    @Column(name = "total_egresos", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalEgresos = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private Estado estado = Estado.Abierta;

    @Column(length = 255)
    private String observaciones;

    @Column(name = "apertura", updatable = false)
    private LocalDateTime apertura = LocalDateTime.now();

    @Column(name = "cierre")
    private LocalDateTime cierre;

    public enum Estado { Abierta, Cerrada }

    public Integer getId()                        { return id; }
    public void    setId(Integer id)              { this.id = id; }

    public Usuario getUsuario()                   { return usuario; }
    public void    setUsuario(Usuario usuario)    { this.usuario = usuario; }

    public BigDecimal getMontoInicial()                    { return montoInicial; }
    public void       setMontoInicial(BigDecimal v)        { this.montoInicial = v; }

    public BigDecimal getMontoFinal()                      { return montoFinal; }
    public void       setMontoFinal(BigDecimal v)          { this.montoFinal = v; }

    public BigDecimal getTotalVentas()                     { return totalVentas; }
    public void       setTotalVentas(BigDecimal v)         { this.totalVentas = v; }

    public BigDecimal getTotalEgresos()                    { return totalEgresos; }
    public void       setTotalEgresos(BigDecimal v)        { this.totalEgresos = v; }

    public Estado  getEstado()                    { return estado; }
    public void    setEstado(Estado estado)       { this.estado = estado; }

    public String  getObservaciones()                  { return observaciones; }
    public void    setObservaciones(String o)          { this.observaciones = o; }

    public LocalDateTime getApertura()                 { return apertura; }
    public void          setApertura(LocalDateTime a)  { this.apertura = a; }

    public LocalDateTime getCierre()                   { return cierre; }
    public void          setCierre(LocalDateTime c)    { this.cierre = c; }
}
