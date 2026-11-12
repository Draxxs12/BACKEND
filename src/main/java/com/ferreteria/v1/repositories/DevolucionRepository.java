package com.ferreteria.v1.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ferreteria.v1.models.Devolucion;

@Repository
public interface DevolucionRepository extends JpaRepository<Devolucion, Integer> {
    List<Devolucion> findAllByOrderByCreatedAtDesc();

    @Query("SELECT SUM(d.cantidad) FROM DetalleDevolucion d "
            + "WHERE d.devolucion.venta.id = :ventaId AND d.producto.id = :productoId")
    Long cantidadDevueltaPorVentaYProducto(
            @Param("ventaId") Integer ventaId,
            @Param("productoId") Integer productoId);
}
