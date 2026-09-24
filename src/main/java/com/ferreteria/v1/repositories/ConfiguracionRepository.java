package com.ferreteria.v1.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ferreteria.v1.models.Configuracion;

@Repository
public interface ConfiguracionRepository extends JpaRepository<Configuracion, Integer> {
}
