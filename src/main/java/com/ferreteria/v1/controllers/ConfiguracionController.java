package com.ferreteria.v1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.ferreteria.v1.models.Configuracion;
import com.ferreteria.v1.repositories.ConfiguracionRepository;

@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionController {

    @Autowired
    private ConfiguracionRepository configuracionRepository;

    /** Devuelve la configuración (crea una por defecto si no existe). */
    @GetMapping
    public Configuracion obtener() {
        return configuracionRepository.findAll().stream().findFirst()
                .orElseGet(() -> configuracionRepository.save(new Configuracion()));
    }

    @PutMapping
    public Configuracion guardar(@RequestBody Configuracion datos) {
        Configuracion cfg = configuracionRepository.findAll().stream().findFirst()
                .orElseGet(Configuracion::new);
        cfg.setNombreEmpresa(datos.getNombreEmpresa());
        cfg.setDireccion(datos.getDireccion());
        cfg.setRuc(datos.getRuc());
        cfg.setSerieBoleta(datos.getSerieBoleta());
        cfg.setSerieFactura(datos.getSerieFactura());
        return configuracionRepository.save(cfg);
    }
}
