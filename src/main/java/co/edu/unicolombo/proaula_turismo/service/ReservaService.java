package co.edu.unicolombo.proaula_turismo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicolombo.proaula_turismo.model.Itinerario;
import co.edu.unicolombo.proaula_turismo.model.Reserva;
import co.edu.unicolombo.proaula_turismo.repository.ItinerarioRepository;
import co.edu.unicolombo.proaula_turismo.repository.ReservaRepository;

@Service
public class ReservaService {

    private final ReservaRepository reservas;
    private final ItinerarioRepository itinerarios;
    private final ItinerarioService itinerarioService;

    public ReservaService(ReservaRepository reservas, ItinerarioRepository itinerarios,
            ItinerarioService itinerarioService) {
        this.reservas = reservas;
        this.itinerarios = itinerarios;
        this.itinerarioService = itinerarioService;
    }

    /** Pago simulado: no se contacta a ninguna pasarela real. */
    @Transactional
    public Reserva pagar(Integer idItinerario, String metodoPago, Integer idCliente) {
        Itinerario itinerario = itinerarioService.obtener(idItinerario, idCliente);
        if (!ItinerarioService.PENDIENTE_PAGO.equals(itinerario.getEstadoItinerario())) {
            throw new IllegalArgumentException("Este itinerario ya fue pagado o cancelado.");
        }
        if (reservas.findByIdItinerario(idItinerario).isPresent()) {
            throw new IllegalArgumentException("Este itinerario ya tiene una reserva.");
        }
        if (metodoPago == null || metodoPago.isBlank()) {
            throw new IllegalArgumentException("Elige un metodo de pago.");
        }

        Reserva reserva = new Reserva();
        reserva.setFechaReserva(LocalDateTime.now());
        reserva.setValorTotal(itinerario.getCostoTotal());
        reserva.setMetodoPago(metodoPago);
        reserva.setEstadoPago("Aprobado");
        reserva.setEstadoReserva("Pagada");
        reserva.setValorReembolso(BigDecimal.ZERO);
        reserva.setIdItinerario(idItinerario);
        reservas.save(reserva);

        itinerario.setEstadoItinerario(ItinerarioService.RESERVADO);
        itinerarios.save(itinerario);
        return reserva;
    }

    public Reserva obtener(Integer idReserva, Integer idCliente) {
        Reserva reserva = reservas.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("La reserva no existe."));
        itinerarioService.obtener(reserva.getIdItinerario(), idCliente);
        return reserva;
    }
}
