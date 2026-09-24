package com.ferreteria.v1.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
@Table(name = "movimientos_caja")
public class MovimientoCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "caja_id", nullable = false)
    private Caja caja;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private Tipo tipo;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal monto;

    @Column(length = 255)
    private String descripcion;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Tipo { Ingreso, Egreso }

    public Integer getId()                       { return id; }
    public void    setId(Integer id)             { this.id = id; }

    public Caja    getCaja()                     { return caja; }
    public void    setCaja(Caja caja)            { this.caja = caja; }

    public Tipo    getTipo()                     { return tipo; }
    public void    setTipo(Tipo tipo)            { this.tipo = tipo; }

    public BigDecimal getMonto()                 { return monto; }
    public void       setMonto(BigDecimal m)     { this.monto = m; }

    public String  getDescripcion()              { return descripcion; }
    public void    setDescripcion(String d)      { this.descripcion = d; }

    public LocalDateTime getCreatedAt()                  { return createdAt; }
    public void          setCreatedAt(LocalDateTime c)   { this.createdAt = c; }
}
