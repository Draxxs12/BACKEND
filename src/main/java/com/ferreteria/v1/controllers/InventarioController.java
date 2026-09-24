package com.ferreteria.v1.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ferreteria.v1.models.Inventario;
import com.ferreteria.v1.models.Usuario;
import com.ferreteria.v1.services.InventarioService;
import com.ferreteria.v1.services.UsuarioService;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<Inventario> listar() {
        return inventarioService.listarTodos();
    }

    @GetMapping("/producto/{productoId}")
    public List<Inventario> porProducto(@PathVariable Integer productoId) {
        return inventarioService.listarPorProducto(productoId);
    }

    @PostMapping
    public Inventario registrar(@RequestBody Inventario inventario) {
        return inventarioService.guardar(inventario);
    }

    @PostMapping("/ajuste")
    public ResponseEntity<?> ajustar(@RequestBody InventarioAjusteRequest req) {
        try {
            Usuario usuario = usuarioService.buscarPorId(req.getUsuarioId());
            if (usuario == null) return ResponseEntity.badRequest().body("Usuario no encontrado.");
            Inventario mov = inventarioService.ajustarStock(
                    req.getProductoId(), req.getNuevoStock(), req.getMotivo(), usuario);
            return ResponseEntity.ok(mov);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
