package co.edu.unicolombo.proaula_turismo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicolombo.proaula_turismo.model.Agencia;

public interface AgenciaRepository extends JpaRepository<Agencia, Integer> {

    // Consulta a la BD: ya hay agencia con este NIT?
    boolean existsByNit(String nit);
}
