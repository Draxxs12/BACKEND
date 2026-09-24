package com.ferreteria.v1.controllers;

/** Cuerpo para un ajuste manual de stock. */
public class InventarioAjusteRequest {
    private Integer usuarioId;
    private Integer productoId;
    private Integer nuevoStock;
    private String  motivo;

    public Integer getUsuarioId()           { return usuarioId; }
    public void    setUsuarioId(Integer id) { this.usuarioId = id; }

    public Integer getProductoId()           { return productoId; }
    public void    setProductoId(Integer id) { this.productoId = id; }

    public Integer getNuevoStock()           { return nuevoStock; }
    public void    setNuevoStock(Integer s)  { this.nuevoStock = s; }

    public String  getMotivo()               { return motivo; }
    public void    setMotivo(String m)       { this.motivo = m; }
}
