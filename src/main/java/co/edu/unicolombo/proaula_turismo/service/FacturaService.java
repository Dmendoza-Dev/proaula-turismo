package co.edu.unicolombo.proaula_turismo.service;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import co.edu.unicolombo.proaula_turismo.model.Itinerario;
import co.edu.unicolombo.proaula_turismo.model.Reserva;
import co.edu.unicolombo.proaula_turismo.model.Usuario;
import co.edu.unicolombo.proaula_turismo.repository.UsuarioRepository;
import co.edu.unicolombo.proaula_turismo.service.dto.FacturaDatos;

@Service
public class FacturaService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy, HH:mm");

    private final ReservaService reservas;
    private final ItinerarioService itinerarios;
    private final UsuarioRepository usuarios;

    public FacturaService(ReservaService reservas, ItinerarioService itinerarios,
            UsuarioRepository usuarios) {
        this.reservas = reservas;
        this.itinerarios = itinerarios;
        this.usuarios = usuarios;
    }

    public FacturaDatos construir(Integer idReserva, Integer idCliente) {
        Reserva reserva = reservas.obtener(idReserva, idCliente);
        Itinerario itinerario = itinerarios.obtener(reserva.getIdItinerario(), idCliente);
        Usuario cliente = usuarios.findById(idCliente)
                .orElseThrow(() -> new IllegalArgumentException("El cliente no existe."));

        String numero = String.format("CC-%d-%04d", reserva.getFechaReserva().getYear(), reserva.getIdReserva());
        return new FacturaDatos(numero, cliente, itinerario, reserva,
                itinerarios.items(itinerario.getIdItinerario()), reserva.getValorTotal());
    }

    public String formatearFecha(java.time.LocalDateTime fecha) {
        return fecha.format(FECHA);
    }
}
