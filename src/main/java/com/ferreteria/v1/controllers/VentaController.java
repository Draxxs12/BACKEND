package com.ferreteria.v1.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ferreteria.v1.models.DetalleVenta;
import com.ferreteria.v1.models.Usuario;
import com.ferreteria.v1.models.Venta;
import com.ferreteria.v1.repositories.VentaRepository;
import com.ferreteria.v1.services.UsuarioService;
import com.ferreteria.v1.services.VentaService;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private VentaRepository ventaRepository;

    @GetMapping
    public List<Venta> listar() {
        return ventaService.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venta> buscar(@PathVariable Integer id) {
        Venta v = ventaService.buscarPorId(id);
        return v != null ? ResponseEntity.ok(v) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/detalle")
    public List<DetalleVenta> detalle(@PathVariable Integer id) {
        return ventaService.obtenerDetalle(id);
    }

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody VentaRequest request) {
        try {
            Usuario usuario = usuarioService.buscarPorId(request.getUsuarioId());
            if (usuario == null) {
                return ResponseEntity.badRequest().body("Usuario no encontrado.");
            }
            Venta resultado = ventaService.registrarVenta(
                request.getVenta(),
                request.getDetalles(),
                usuario
            );
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/anular")
    public ResponseEntity<?> anular(@PathVariable Integer id, @RequestParam Integer usuarioId) {
        try {
            Usuario usuario = usuarioService.buscarPorId(usuarioId);
            if (usuario == null) return ResponseEntity.badRequest().body("Usuario no encontrado.");
            return ResponseEntity.ok(ventaService.anular(id, usuario));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/top-clientes")
    public ResponseEntity<List<Map<String, Object>>> topClientes() {
        List<Object[]> rows = ventaRepository.topClientesPorVentas();
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> item = new HashMap<>();
            item.put("nombre", row[0]);
            item.put("total",  row[1]);
            resultado.add(item);
        }
        return ResponseEntity.ok(resultado);
    }
}