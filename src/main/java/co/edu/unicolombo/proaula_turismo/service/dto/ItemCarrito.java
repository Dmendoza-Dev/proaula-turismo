package co.edu.unicolombo.proaula_turismo.service.dto;


// Cada linea del carrito: que oferta y cuantas unidades quiere el turista.
public record ItemCarrito(Integer idOferta, Integer cantidad) {
}
