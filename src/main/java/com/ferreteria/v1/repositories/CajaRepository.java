package com.ferreteria.v1.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ferreteria.v1.models.Caja;

@Repository
public interface CajaRepository extends JpaRepository<Caja, Integer> {
    Optional<Caja> findFirstByEstadoOrderByAperturaDesc(Caja.Estado estado);
    List<Caja> findAllByOrderByAperturaDesc();
}
