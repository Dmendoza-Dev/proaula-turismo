package co.edu.unicolombo.proaula_turismo.service;

import java.util.List;


public final class MetodosPago {

    public static final List<String> PERMITIDOS = List.of("Tarjeta de crédito", "PSE", "Efectivo");

    private MetodosPago() {
    }

    
    // Compara sin importar mayusculas ni espacios y devuelve el nombre oficial.
    public static String normalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Elige un metodo de pago.");
        }
        String limpio = valor.trim();
        // stream recorre la lista permitida; findFirst se queda con la primera coincidencia.
        return PERMITIDOS.stream()
                .filter(metodo -> metodo.equalsIgnoreCase(limpio))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Metodo de pago no valido. Elige Tarjeta de credito, PSE o Efectivo."));
    }
}
