package com.ferreteria.v1.controllers;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import com.ferreteria.v1.services.IaService;
import com.ferreteria.v1.services.ReporteService;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    @Autowired private ReporteService reporteService;
    @Autowired private IaService iaService;

    @GetMapping("/libro-mayor")
    public Map<String, Object> libroMayor(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteService.libroMayor(desde, hasta);
    }

    @GetMapping("/stock")
    public Map<String, Object> auditoriaStock() {
        return reporteService.auditoriaStock();
    }

    @GetMapping("/analisis-ia")
    public Map<String, Object> analisisIa(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return iaService.analizar(desde, hasta);
    }

    @GetMapping("/analisis-ia-stock")
    public Map<String, Object> analisisIaStock() {
        return iaService.analizarStock();
    }
}
