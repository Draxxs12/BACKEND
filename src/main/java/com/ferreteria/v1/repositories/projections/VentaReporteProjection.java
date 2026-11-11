package com.ferreteria.v1.repositories.projections;

import java.time.LocalDateTime;

/** Proyección ligera: evita hidratar entidades y relaciones que el reporte no utiliza. */
public interface VentaReporteProjection {
    LocalDateTime getFecha();
    String getComprobante();
    String getCliente();
    Double getTotal();
}
