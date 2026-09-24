package com.ferreteria.v1.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ferreteria.v1.models.Devolucion;
import com.ferreteria.v1.models.Usuario;
import com.ferreteria.v1.services.DevolucionService;
import com.ferreteria.v1.services.UsuarioService;

@RestController
@RequestMapping("/api/devoluciones")
public class DevolucionController {

    @Autowired private DevolucionService devolucionService;
    @Autowired private UsuarioService usuarioService;

    @GetMapping
    public List<Devolucion> listar() {
        return devolucionService.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Devolucion> buscar(@PathVariable Integer id) {
        Devolucion d = devolucionService.buscarPorId(id);
        return d != null ? ResponseEntity.ok(d) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody DevolucionRequest req) {
        try {
            Usuario usuario = usuarioService.buscarPorId(req.getUsuarioId());
            if (usuario == null) return ResponseEntity.badRequest().body("Usuario no encontrado.");
            if (req.getDetalles() == null || req.getDetalles().isEmpty()) {
                return ResponseEntity.badRequest().body("Debes indicar al menos un producto a devolver.");
            }
            Devolucion dev = devolucionService.registrar(
                    req.getVentaId(), req.getMotivo(), req.getTipoReembolso(),
                    req.getDetalles(), usuario);
            return ResponseEntity.ok(dev);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
