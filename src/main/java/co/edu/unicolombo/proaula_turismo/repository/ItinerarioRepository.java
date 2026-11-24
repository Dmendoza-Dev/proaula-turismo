package co.edu.unicolombo.proaula_turismo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicolombo.proaula_turismo.model.Itinerario;

public interface ItinerarioRepository extends JpaRepository<Itinerario, Integer> {

    List<Itinerario> findByIdClienteOrderByIdItinerarioDesc(Integer idCliente);
}
