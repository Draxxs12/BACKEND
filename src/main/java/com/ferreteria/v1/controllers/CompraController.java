package com.ferreteria.v1.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ferreteria.v1.models.Compra;
import com.ferreteria.v1.services.CompraService;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

    @Autowired
    private CompraService compraService;

    @GetMapping
    public List<Compra> listar() {
        return compraService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Compra> buscar(@PathVariable Integer id) {
        Compra c = compraService.buscarPorId(id);
        return c != null ? ResponseEntity.ok(c) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public Compra crear(@RequestBody Compra compra) {
        return compraService.guardar(compra);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Compra> actualizar(@PathVariable Integer id,
                                             @RequestBody Compra compra) {
        compra.setId(id);
        return ResponseEntity.ok(compraService.guardar(compra));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        compraService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
    
    
}
