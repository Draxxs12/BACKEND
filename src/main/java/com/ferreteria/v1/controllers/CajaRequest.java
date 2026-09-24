package com.ferreteria.v1.controllers;

import java.math.BigDecimal;

/** Cuerpo para abrir/cerrar caja. */
public class CajaRequest {
    private Integer usuarioId;
    private BigDecimal montoInicial;
    private String observaciones;

    public Integer getUsuarioId()              { return usuarioId; }
    public void    setUsuarioId(Integer id)    { this.usuarioId = id; }

    public BigDecimal getMontoInicial()             { return montoInicial; }
    public void       setMontoInicial(BigDecimal m) { this.montoInicial = m; }

    public String getObservaciones()           { return observaciones; }
    public void   setObservaciones(String o)   { this.observaciones = o; }
}
