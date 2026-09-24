package com.ferreteria.v1.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ferreteria.v1.models.Caja;
import com.ferreteria.v1.models.MovimientoCaja;
import com.ferreteria.v1.models.Usuario;
import com.ferreteria.v1.repositories.CajaRepository;
import com.ferreteria.v1.repositories.MovimientoCajaRepository;

@Service
public class CajaService {

    @Autowired private CajaRepository cajaRepository;
    @Autowired private MovimientoCajaRepository movimientoRepository;

    public Caja cajaActual() {
        return cajaRepository.findFirstByEstadoOrderByAperturaDesc(Caja.Estado.Abierta).orElse(null);
    }

    public List<Caja> historial() {
        return cajaRepository.findAllByOrderByAperturaDesc();
    }

    public List<MovimientoCaja> movimientos(Integer cajaId) {
        return movimientoRepository.findByCajaIdOrderByCreatedAtDesc(cajaId);
    }

    @Transactional
    public Caja abrir(BigDecimal montoInicial, Usuario usuario) {
        if (cajaActual() != null) {
            throw new RuntimeException("Ya existe una caja abierta. Ciérrala antes de abrir otra.");
        }
        Caja caja = new Caja();
        caja.setUsuario(usuario);
        caja.setMontoInicial(montoInicial != null ? montoInicial : BigDecimal.ZERO);
        caja.setEstado(Caja.Estado.Abierta);
        caja.setApertura(LocalDateTime.now());
        return cajaRepository.save(caja);
    }

    @Transactional
    public Caja cerrar(String observaciones) {
        Caja caja = cajaActual();
        if (caja == null) {
            throw new RuntimeException("No hay ninguna caja abierta.");
        }
        BigDecimal montoFinal = caja.getMontoInicial()
                .add(caja.getTotalVentas())
                .subtract(caja.getTotalEgresos());
        caja.setMontoFinal(montoFinal);
        caja.setEstado(Caja.Estado.Cerrada);
        caja.setCierre(LocalDateTime.now());
        if (observaciones != null && !observaciones.isBlank()) {
            caja.setObservaciones(observaciones);
        }
        return cajaRepository.save(caja);
    }

    @Transactional
    public MovimientoCaja registrarMovimiento(MovimientoCaja mov) {
        Caja caja = cajaActual();
        if (caja == null) {
            throw new RuntimeException("Debes abrir la caja antes de registrar movimientos.");
        }
        mov.setCaja(caja);
        if (mov.getTipo() == MovimientoCaja.Tipo.Egreso) {
            caja.setTotalEgresos(caja.getTotalEgresos().add(mov.getMonto()));
        } else {
            caja.setTotalVentas(caja.getTotalVentas().add(mov.getMonto()));
        }
        cajaRepository.save(caja);
        return movimientoRepository.save(mov);
    }
}
