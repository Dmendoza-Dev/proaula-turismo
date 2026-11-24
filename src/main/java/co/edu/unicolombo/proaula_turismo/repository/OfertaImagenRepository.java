package co.edu.unicolombo.proaula_turismo.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicolombo.proaula_turismo.model.OfertaImagen;

public interface OfertaImagenRepository extends JpaRepository<OfertaImagen, Integer> {

    // Consulta a la BD: fotos de una oferta.
    List<OfertaImagen> findByIdOferta(Integer idOferta);

    // Consulta a la BD: fotos de varias ofertas en una sola ida.
    List<OfertaImagen> findByIdOfertaIn(Collection<Integer> ids);
}
