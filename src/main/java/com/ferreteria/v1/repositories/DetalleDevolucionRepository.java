package com.ferreteria.v1.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ferreteria.v1.models.DetalleDevolucion;

@Repository
public interface DetalleDevolucionRepository extends JpaRepository<DetalleDevolucion, Integer> {
    List<DetalleDevolucion> findByDevolucionId(Integer devolucionId);
}
