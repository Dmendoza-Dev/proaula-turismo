package co.edu.unicolombo.proaula_turismo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicolombo.proaula_turismo.model.Reserva;

public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    // Consulta a la BD: este itinerario ya tiene su reserva?
    Optional<Reserva> findByIdItinerario(Integer idItinerario);
}
