package co.edu.unicolombo.proaula_turismo.service.dto;

import java.math.BigDecimal;

/** Detalle del itinerario con el nombre de la oferta ya resuelto. */
public record ItemItinerario(
        String nombreOferta,
        String categoria,
        String isla,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal) {
}
