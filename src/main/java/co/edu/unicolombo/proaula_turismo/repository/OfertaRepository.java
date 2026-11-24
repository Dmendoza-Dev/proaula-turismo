package co.edu.unicolombo.proaula_turismo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicolombo.proaula_turismo.model.Oferta;

public interface OfertaRepository extends JpaRepository<Oferta, Integer> {

    List<Oferta> findByEstadoOfertaOrderByIdOfertaDesc(String estadoOferta);

    List<Oferta> findByIdProveedorOrderByIdOfertaDesc(Integer idProveedor);
}
