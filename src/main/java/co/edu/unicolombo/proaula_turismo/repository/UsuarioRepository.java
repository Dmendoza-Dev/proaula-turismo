package co.edu.unicolombo.proaula_turismo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicolombo.proaula_turismo.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Consulta a la BD: busca usuario por correo para el login.
    Optional<Usuario> findByCorreo(String correo);

    // Consulta a la BD: ya existe ese correo?
    boolean existsByCorreo(String correo);

    // Consulta a la BD: ya existe ese documento?
    boolean existsByDocumento(String documento);
}
