package com.ferreteria.v1.repositories.projections;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Proyección ligera para el detalle financiero de compras. */
public interface CompraReporteProjection {
    LocalDateTime getFecha();
    String getOrden();
    String getProveedor();
    String getEstado();
    BigDecimal getTotal();
}
