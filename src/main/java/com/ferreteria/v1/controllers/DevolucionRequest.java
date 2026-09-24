package com.ferreteria.v1.controllers;

import java.util.List;

import com.ferreteria.v1.models.DetalleDevolucion;
import com.ferreteria.v1.models.Devolucion;

/** Cuerpo para registrar una devolución / nota de crédito. */
public class DevolucionRequest {
    private Integer usuarioId;
    private Integer ventaId;
    private String  motivo;
    private Devolucion.TipoReembolso tipoReembolso;
    private List<DetalleDevolucion> detalles;

    public Integer getUsuarioId()           { return usuarioId; }
    public void    setUsuarioId(Integer id) { this.usuarioId = id; }

    public Integer getVentaId()             { return ventaId; }
    public void    setVentaId(Integer v)    { this.ventaId = v; }

    public String  getMotivo()              { return motivo; }
    public void    setMotivo(String m)      { this.motivo = m; }

    public Devolucion.TipoReembolso getTipoReembolso()                 { return tipoReembolso; }
    public void setTipoReembolso(Devolucion.TipoReembolso t)           { this.tipoReembolso = t; }

    public List<DetalleDevolucion> getDetalles()                  { return detalles; }
    public void setDetalles(List<DetalleDevolucion> d)            { this.detalles = d; }
}
