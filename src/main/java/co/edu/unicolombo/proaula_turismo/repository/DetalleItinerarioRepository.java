package co.edu.unicolombo.proaula_turismo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicolombo.proaula_turismo.model.DetalleItinerario;

public interface DetalleItinerarioRepository extends JpaRepository<DetalleItinerario, Integer> {

    List<DetalleItinerario> findByIdItinerario(Integer idItinerario);

    void deleteByIdItinerario(Integer idItinerario);
}
