package com.ferreteria.v1.credito;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** HU-S3-02 (RF-16): estado de mora de un cliente (AL_DIA, POR_VENCER, MOROSO). */
class MoraClienteServiceTest {

    private final MoraClienteService service = new MoraClienteService();
    private final LocalDate hoy = LocalDate.of(2026, 10, 20);
    private final BigDecimal deuda = new BigDecimal("150.00");

    @Test
    void clienteConSaldoYFacturaVencidaHaceCincoDiasEsMoroso() {
        EstadoMora estado = service.calcularEstado(deuda, hoy.minusDays(5), hoy);

        assertEquals(EstadoMora.MOROSO, estado);
    }

    @Test
    void facturaVencidaAyerEsMorosa_valorLimite() {
        assertEquals(EstadoMora.MOROSO, service.calcularEstado(deuda, hoy.minusDays(1), hoy));
    }

    @Test
    void facturaQueVenceHoyEstaPorVencer_valorLimite() {
        assertEquals(EstadoMora.POR_VENCER, service.calcularEstado(deuda, hoy, hoy));
    }

    @Test
    void facturaQueVenceEnTresDiasEstaPorVencer_valorLimite() {
        assertEquals(EstadoMora.POR_VENCER, service.calcularEstado(deuda, hoy.plusDays(3), hoy));
    }

    @Test
    void facturaQueVenceEnCuatroDiasEstaAlDia_valorLimite() {
        assertEquals(EstadoMora.AL_DIA, service.calcularEstado(deuda, hoy.plusDays(4), hoy));
    }

    @Test
    void clienteSinSaldoEstaAlDiaAunqueLaFacturaEsteVencida() {
        assertEquals(EstadoMora.AL_DIA, service.calcularEstado(BigDecimal.ZERO, hoy.minusDays(30), hoy));
        assertEquals(EstadoMora.AL_DIA, service.calcularEstado(null, hoy.minusDays(30), hoy));
    }
}
