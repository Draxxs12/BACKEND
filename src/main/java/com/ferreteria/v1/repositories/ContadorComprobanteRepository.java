package com.ferreteria.v1.repositories;

import com.ferreteria.v1.models.ContadorComprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContadorComprobanteRepository extends JpaRepository<ContadorComprobante, String> {

    @Modifying
    @Query(value = "INSERT INTO contador_comprobante (serie, ultimo_numero) "
            + "SELECT :serie, LAST_INSERT_ID(COALESCE(MAX(CAST(SUBSTRING(numero_comprobante, 6) "
            + "AS UNSIGNED)), 0) + 1) FROM ventas "
            + "WHERE numero_comprobante LIKE CONCAT(:serie, '-%') "
            + "ON DUPLICATE KEY UPDATE ultimo_numero = LAST_INSERT_ID(ultimo_numero + 1)",
            nativeQuery = true)
    void reservarSiguiente(@Param("serie") String serie);

    @Query(value = "SELECT LAST_INSERT_ID()", nativeQuery = true)
    Long obtenerNumeroReservado();
}
