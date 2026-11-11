package com.ferreteria.v1.repositories.projections;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.ferreteria.v1.models.Compra;

/** Proyección ligera para el detalle financiero de compras. */
public interface CompraReporteProjection {
    LocalDateTime getFecha();
    String getOrden();
    String getProveedor();
    Compra.Estado getEstado();
    BigDecimal getTotal();
}
