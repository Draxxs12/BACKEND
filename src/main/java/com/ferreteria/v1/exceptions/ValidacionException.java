package com.ferreteria.v1.exceptions;

/** Se lanza cuando un dato no cumple las reglas de negocio (precio negativo, RUC inválido, etc.). */
public class ValidacionException extends RuntimeException {
    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
