package co.edu.unicolombo.proaula_turismo.service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import co.edu.unicolombo.proaula_turismo.model.Itinerario;
import co.edu.unicolombo.proaula_turismo.model.Reserva;
import co.edu.unicolombo.proaula_turismo.model.Usuario;


// record = paquete de datos listo para leer; aqui junta todo lo que ve la factura.
public record FacturaDatos(
        String numeroReserva,
        Usuario cliente,
        Itinerario itinerario,
        Reserva reserva,
        List<ItemItinerario> items,
        BigDecimal total) {

    public LocalDateTime fechaReserva() {
        return reserva.getFechaReserva();
    }
}
