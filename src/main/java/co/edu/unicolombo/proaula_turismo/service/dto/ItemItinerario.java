package co.edu.unicolombo.proaula_turismo.service.dto;

import java.math.BigDecimal;


// Igual que el carrito, pero ya con nombres y precios guardados en la BD.
public record ItemItinerario(
        String nombreOferta,
        String categoria,
        String isla,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal) {
}
