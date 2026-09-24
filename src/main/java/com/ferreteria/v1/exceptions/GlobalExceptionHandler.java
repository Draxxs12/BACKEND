package com.ferreteria.v1.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Captura las excepciones de toda la aplicación y devuelve un mensaje claro al frontend.
 * Centraliza el manejo de errores: ningún controlador necesita repetir try/catch.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Reglas de negocio (precio negativo, RUC/email inválido, etc.) → 400. */
    @ExceptionHandler(ValidacionException.class)
    public ResponseEntity<String> manejarValidacion(ValidacionException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    /** Recurso inexistente → 404. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<String> manejarNoEncontrado(RecursoNoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    /** Argumentos inválidos → 400. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> manejarIlegal(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
