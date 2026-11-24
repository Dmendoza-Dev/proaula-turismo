package co.edu.unicolombo.proaula_turismo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicolombo.proaula_turismo.model.Oferta;

public interface OfertaRepository extends JpaRepository<Oferta, Integer> {

    // Consulta a la BD: ofertas en un estado, primero las mas recientes.
    List<Oferta> findByEstadoOfertaOrderByIdOfertaDesc(String estadoOferta);

    // Consulta a la BD: ofertas de un publicador, primero las mas recientes.
    List<Oferta> findByIdProveedorOrderByIdOfertaDesc(Integer idProveedor);
}
