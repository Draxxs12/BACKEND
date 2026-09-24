package com.ferreteria.v1.exceptions;

/** Se lanza cuando no se encuentra un recurso solicitado (id inexistente). */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
