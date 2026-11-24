package co.edu.unicolombo.proaula_turismo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicolombo.proaula_turismo.model.DetalleItinerario;

public interface DetalleItinerarioRepository extends JpaRepository<DetalleItinerario, Integer> {

    // Consulta a la BD: trae las ofertas que forman un itinerario.
    List<DetalleItinerario> findByIdItinerario(Integer idItinerario);

    // Consulta a la BD: esta oferta aparece en algun itinerario?
    boolean existsByIdOferta(Integer idOferta);
}
