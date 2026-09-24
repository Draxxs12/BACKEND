package com.ferreteria.v1.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ferreteria.v1.models.Rol;
import com.ferreteria.v1.repositories.RolRepository;

@RestController
@RequestMapping("/api/roles")
public class RolController {

    @Autowired
    private RolRepository rolRepository;

    @GetMapping
    public List<Rol> listar() {
        return rolRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Rol rol) {
        if (rol.getNombre() == null || rol.getNombre().isBlank()) {
            return ResponseEntity.badRequest().body("El nombre del rol es obligatorio.");
        }
        if (rolRepository.findByNombre(rol.getNombre()).isPresent()) {
            return ResponseEntity.badRequest().body("Ya existe un rol con ese nombre.");
        }
        return ResponseEntity.ok(rolRepository.save(rol));
    }
}
