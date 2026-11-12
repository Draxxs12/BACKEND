package com.ferreteria.v1.repositories.projections;

/** Columnas necesarias para auditoría; no carga la entidad Producto completa. */
public interface StockReporteProjection {
    String getCodigo();
    String getNombre();
    String getCategoria();
    Integer getStock();
    Integer getStockMinimo();
    Double getPrecioCompra();
}
