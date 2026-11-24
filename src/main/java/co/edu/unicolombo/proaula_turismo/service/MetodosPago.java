package co.edu.unicolombo.proaula_turismo.service;

import java.util.List;

/** Metodos de pago que acepta el pago simulado. */
public final class MetodosPago {

    public static final List<String> PERMITIDOS = List.of("Tarjeta de crédito", "PSE", "Efectivo");

    private MetodosPago() {
    }

    /** Devuelve el nombre canonico del metodo o lanza un error entendible. */
    public static String normalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Elige un metodo de pago.");
        }
        String limpio = valor.trim();
        return PERMITIDOS.stream()
                .filter(metodo -> metodo.equalsIgnoreCase(limpio))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Metodo de pago no valido. Elige Tarjeta de credito, PSE o Efectivo."));
    }
}
