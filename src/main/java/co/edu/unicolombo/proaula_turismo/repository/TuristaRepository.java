package co.edu.unicolombo.proaula_turismo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicolombo.proaula_turismo.model.Turista;

public interface TuristaRepository extends JpaRepository<Turista, Integer> {
}
