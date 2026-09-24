package com.ferreteria.v1.exceptions;

import java.util.regex.Pattern;

/** Reglas de validación reutilizables. Lanzan ValidacionException si el dato es inválido. */
public final class Validador {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private Validador() { }

    /** El correo, si viene, debe tener formato válido (algo@algo.algo). */
    public static void email(String email) {
        if (email != null && !email.isBlank() && !EMAIL.matcher(email).matches()) {
            throw new ValidacionException("El correo no tiene un formato válido: " + email);
        }
    }

    /** El valor no puede ser negativo (precios, stock, montos). */
    public static void noNegativo(Number valor, String campo) {
        if (valor != null && valor.doubleValue() < 0) {
            throw new ValidacionException("El " + campo + " no puede ser negativo.");
        }
    }

    /** El precio de venta no puede ser menor que el de compra (evita vender con pérdida). */
    public static void ventaMayorIgualCompra(Double precioVenta, Double precioCompra) {
        if (precioVenta != null && precioCompra != null && precioVenta < precioCompra) {
            throw new ValidacionException(
                "El precio de venta (S/ " + precioVenta + ") no puede ser menor que el precio de compra (S/ "
                + precioCompra + "). Estarías vendiendo con pérdida.");
        }
    }

    /** El RUC, si viene, debe ser solo dígitos y máximo 20 caracteres. */
    public static void ruc(String ruc) {
        if (ruc != null && !ruc.isBlank() && !ruc.matches("\\d{1,20}")) {
            throw new ValidacionException("El RUC debe contener solo números y un máximo de 20 dígitos.");
        }
    }

    /** Documento según el tipo: DNI = 8 dígitos, RUC = 20 dígitos. */
    public static void documento(String tipo, String numero) {
        if (numero == null || numero.isBlank()) return;
        if ("DNI".equals(tipo) && !numero.matches("\\d{8}")) {
            throw new ValidacionException("El DNI debe tener exactamente 8 dígitos numéricos.");
        }
        if ("RUC".equals(tipo) && !numero.matches("\\d{20}")) {
            throw new ValidacionException("El RUC debe tener exactamente 20 dígitos numéricos.");
        }
    }
}
