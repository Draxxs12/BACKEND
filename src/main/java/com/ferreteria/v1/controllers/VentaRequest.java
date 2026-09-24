package com.ferreteria.v1.controllers;
 
import com.ferreteria.v1.models.DetalleVenta;
import com.ferreteria.v1.models.Venta;
import java.util.List;
 
public class VentaRequest {
 
    private Integer usuarioId;
    private Venta venta;
    private List<DetalleVenta> detalles;
 
    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }
 
    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }
 
    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }
}
 