package com.ferreteria.v1.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ferreteria.v1.models.Caja;
import com.ferreteria.v1.models.MovimientoCaja;
import com.ferreteria.v1.models.Usuario;
import com.ferreteria.v1.services.CajaService;
import com.ferreteria.v1.services.UsuarioService;

@RestController
@RequestMapping("/api/caja")
public class CajaController {

    @Autowired private CajaService cajaService;
    @Autowired private UsuarioService usuarioService;

    @GetMapping("/actual")
    public ResponseEntity<Caja> actual() {
        return ResponseEntity.ok(cajaService.cajaActual());
    }

    @GetMapping
    public List<Caja> historial() {
        return cajaService.historial();
    }

    @GetMapping("/{id}/movimientos")
    public List<MovimientoCaja> movimientos(@PathVariable Integer id) {
        return cajaService.movimientos(id);
    }

    @PostMapping("/abrir")
    public ResponseEntity<?> abrir(@RequestBody CajaRequest req) {
        try {
            Usuario usuario = usuarioService.buscarPorId(req.getUsuarioId());
            if (usuario == null) return ResponseEntity.badRequest().body("Usuario no encontrado.");
            return ResponseEntity.ok(cajaService.abrir(req.getMontoInicial(), usuario));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/cerrar")
    public ResponseEntity<?> cerrar(@RequestBody(required = false) CajaRequest req) {
        try {
            String obs = req != null ? req.getObservaciones() : null;
            return ResponseEntity.ok(cajaService.cerrar(obs));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/movimiento")
    public ResponseEntity<?> movimiento(@RequestBody MovimientoCaja mov) {
        try {
            return ResponseEntity.ok(cajaService.registrarMovimiento(mov));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
