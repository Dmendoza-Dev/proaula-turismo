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

    
    // Si falla el pago, ni la reserva ni el descuento de cupos quedan a medias.
    @Transactional
    public Reserva pagar(Integer idItinerario, String metodoPago, Integer idCliente) {
        Itinerario itinerario = itinerarioService.obtener(idItinerario, idCliente);
        // Evita pagar dos veces el mismo itinerario.
        if (!ItinerarioService.PENDIENTE_PAGO.equals(itinerario.getEstadoItinerario())) {
            throw new IllegalArgumentException("Este itinerario ya fue pagado o cancelado.");
        }
        // Segunda barrera: si ya existe reserva, no se crea otra.
        if (reservas.findByIdItinerario(idItinerario).isPresent()) {
            throw new IllegalArgumentException("Este itinerario ya tiene una reserva.");
        }
        //  pse se convierte en PSE; cualquier otra cosa se rechaza.
        String metodo = MetodosPago.normalizar(metodoPago);

        
        // Aqui se restan las plazas de cada oferta.
        itinerarioService.descontarCupos(idItinerario);

        Reserva reserva = new Reserva();
        reserva.setFechaReserva(LocalDateTime.now());
        reserva.setValorTotal(itinerario.getCostoTotal());
        reserva.setMetodoPago(metodo);
        // El pago es simulado: siempre se aprueba.
        reserva.setEstadoPago("Aprobado");
        reserva.setEstadoReserva("Pagada");
        reserva.setValorReembolso(BigDecimal.ZERO);
        reserva.setIdItinerario(idItinerario);
        reservas.save(reserva);

        // El itinerario pasa a Reservado solo despues de guardar la reserva.
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
