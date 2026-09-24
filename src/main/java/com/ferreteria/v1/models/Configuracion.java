package com.ferreteria.v1.models;

import jakarta.persistence.*;

@Entity
@Table(name = "configuracion")
public class Configuracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre_empresa", length = 200)
    private String nombreEmpresa = "Ferretería Progresol Charito";

    @Column(length = 255)
    private String direccion;

    @Column(length = 20)
    private String ruc;

    @Column(name = "serie_boleta", length = 10)
    private String serieBoleta = "B001";

    @Column(name = "serie_factura", length = 10)
    private String serieFactura = "F001";

    public Integer getId()                    { return id; }
    public void    setId(Integer id)          { this.id = id; }

    public String getNombreEmpresa()              { return nombreEmpresa; }
    public void   setNombreEmpresa(String v)      { this.nombreEmpresa = v; }

    public String getDireccion()              { return direccion; }
    public void   setDireccion(String v)      { this.direccion = v; }

    public String getRuc()                    { return ruc; }
    public void   setRuc(String v)            { this.ruc = v; }

    public String getSerieBoleta()            { return serieBoleta; }
    public void   setSerieBoleta(String v)    { this.serieBoleta = v; }

    public String getSerieFactura()           { return serieFactura; }
    public void   setSerieFactura(String v)   { this.serieFactura = v; }
}
