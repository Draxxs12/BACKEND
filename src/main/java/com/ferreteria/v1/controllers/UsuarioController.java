package com.ferreteria.v1.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ferreteria.v1.models.Usuario;
import com.ferreteria.v1.services.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listarActivos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscar(@PathVariable Integer id) {
        Usuario u = usuarioService.buscarPorId(id);
        return u != null ? ResponseEntity.ok(u) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody UsuarioRequest req) {
        try {
            Usuario u = usuarioService.crear(
                    req.getNombre(), req.getEmail(), req.getPassword(),
                    req.getRolId(), req.getActivo());
            return ResponseEntity.ok(u);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody UsuarioRequest req) {
        try {
            Usuario u = usuarioService.actualizar(
                    id, req.getNombre(), req.getEmail(), req.getPassword(),
                    req.getRolId(), req.getActivo());
            return ResponseEntity.ok(u);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
