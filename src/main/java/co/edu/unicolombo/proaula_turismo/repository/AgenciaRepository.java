package co.edu.unicolombo.proaula_turismo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicolombo.proaula_turismo.model.Agencia;

public interface AgenciaRepository extends JpaRepository<Agencia, Integer> {

    Optional<Agencia> findByIdUsuario(Integer idUsuario);
}
