package co.edu.unicolombo.proaula_turismo.service;

import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import co.edu.unicolombo.proaula_turismo.model.DetalleItinerario;
import co.edu.unicolombo.proaula_turismo.model.Oferta;
import co.edu.unicolombo.proaula_turismo.model.OfertaImagen;
import co.edu.unicolombo.proaula_turismo.repository.DetalleItinerarioRepository;
import co.edu.unicolombo.proaula_turismo.repository.OfertaImagenRepository;
import co.edu.unicolombo.proaula_turismo.repository.OfertaRepository;
import co.edu.unicolombo.proaula_turismo.service.dto.OfertaForm;

@Service
public class OfertaService {

    public static final String PENDIENTE = "Pendiente";
    public static final String PUBLICADA = "Publicada";
    public static final String RECHAZADA = "Rechazada";
    public static final String CANCELADA = "Cancelada";

    private final OfertaRepository ofertas;
    private final OfertaImagenRepository imagenes;
    private final DetalleItinerarioRepository detalles;
    private final ArchivoService archivos;

    public OfertaService(OfertaRepository ofertas, OfertaImagenRepository imagenes,
            DetalleItinerarioRepository detalles, ArchivoService archivos) {
        this.ofertas = ofertas;
        this.imagenes = imagenes;
        this.detalles = detalles;
        this.archivos = archivos;
    }

    @Transactional
    public Oferta crear(OfertaForm datos, Integer idUsuario) {
        Oferta oferta = new Oferta();
        aplicar(datos, oferta);
        oferta.setEstadoOferta(PENDIENTE);
        oferta.setIdProveedor(idUsuario);
        ofertas.save(oferta);
        guardarImagenes(datos.imagenes(), oferta.getIdOferta());
        return oferta;
    }

    @Transactional
    public Oferta actualizar(Integer idOferta, OfertaForm datos, Integer idUsuario) {
        Oferta oferta = ofertas.findById(idOferta)
                .orElseThrow(() -> new IllegalArgumentException("La oferta no existe."));
        if (!oferta.getIdProveedor().equals(idUsuario)) {
            throw new IllegalArgumentException("No puedes editar una oferta de otro publicador.");
        }
        aplicar(datos, oferta);
        // Toda modificacion vuelve a auditoria.
        oferta.setEstadoOferta(PENDIENTE);
        ofertas.save(oferta);
        if (datos.imagenes() != null && !datos.imagenes().isEmpty()) {
            borrarImagenes(idOferta);
            guardarImagenes(datos.imagenes(), idOferta);
        }
        return oferta;
    }

    @Transactional
    public void eliminar(Integer idOferta, Integer idUsuario) {
        Oferta oferta = ofertas.findById(idOferta)
                .orElseThrow(() -> new IllegalArgumentException("La oferta no existe."));
        if (!oferta.getIdProveedor().equals(idUsuario)) {
            throw new IllegalArgumentException("No puedes eliminar una oferta de otro publicador.");
        }
        List<DetalleItinerario> usos = detalles.findAll().stream()
                .filter(d -> d.getIdOferta().equals(idOferta))
                .toList();
        if (!usos.isEmpty()) {
            // Ya forma parte de un itinerario: se retira del catalogo sin borrar el historial.
            oferta.setEstadoOferta(CANCELADA);
            ofertas.save(oferta);
            return;
        }
        borrarImagenes(idOferta);
        ofertas.delete(oferta);
    }

    @Transactional
    public void aprobar(Integer idOferta) {
        cambiarEstado(idOferta, PENDIENTE, PUBLICADA);
    }

    @Transactional
    public void rechazar(Integer idOferta) {
        cambiarEstado(idOferta, PENDIENTE, RECHAZADA);
    }

    private void cambiarEstado(Integer idOferta, String esperado, String nuevo) {
        Oferta oferta = ofertas.findById(idOferta)
                .orElseThrow(() -> new IllegalArgumentException("La oferta no existe."));
        if (!esperado.equals(oferta.getEstadoOferta())) {
            throw new IllegalArgumentException("Esta oferta ya no esta pendiente de auditoria.");
        }
        oferta.setEstadoOferta(nuevo);
        ofertas.save(oferta);
    }

    public List<Oferta> publicadas() {
        return ofertas.findByEstadoOfertaOrderByIdOfertaDesc(PUBLICADA);
    }

    public List<Oferta> porPublicador(Integer idUsuario) {
        return ofertas.findByIdProveedorOrderByIdOfertaDesc(idUsuario);
    }

    public List<Oferta> pendientes() {
        return ofertas.findByEstadoOfertaOrderByIdOfertaDesc(PENDIENTE);
    }

    public List<Oferta> todas() {
        return ofertas.findAll();
    }

    public Oferta buscar(Integer idOferta) {
        return ofertas.findById(idOferta)
                .orElseThrow(() -> new IllegalArgumentException("La oferta no existe."));
    }

    public List<OfertaImagen> imagenes(Integer idOferta) {
        return imagenes.findByIdOferta(idOferta);
    }

    private void aplicar(OfertaForm datos, Oferta oferta) {
        if (datos.nombre() == null || datos.nombre().isBlank()
                || datos.categoria() == null || datos.categoria().isBlank()
                || datos.isla() == null || datos.isla().isBlank()) {
            throw new IllegalArgumentException("Nombre, categoria e isla son obligatorios.");
        }
        if (datos.tarifa() == null || datos.tarifa().signum() <= 0) {
            throw new IllegalArgumentException("La tarifa debe ser mayor a cero.");
        }
        if (datos.cupos() == null || datos.cupos() < 1) {
            throw new IllegalArgumentException("Los cupos deben ser al menos 1.");
        }

        oferta.setNombreOferta(datos.nombre().trim());
        oferta.setDescripcion(datos.descripcion());
        oferta.setCategoria(datos.categoria());
        oferta.setIsla(datos.isla());
        oferta.setPrecio(datos.tarifa());
        oferta.setCuposMaximos(datos.cupos());
        oferta.setDiasDisponibles(datos.dias() == null || datos.dias().isEmpty()
                ? null : String.join(", ", datos.dias()));
        oferta.setHorarioInicio(parsearHora(datos.horarioInicio()));
        oferta.setHorarioFin(parsearHora(datos.horarioFin()));
    }

    private LocalTime parsearHora(String valor) {
        return valor == null || valor.isBlank() ? null : LocalTime.parse(valor);
    }

    private void guardarImagenes(List<MultipartFile> archivosSubidos, Integer idOferta) {
        if (archivosSubidos == null) {
            return;
        }
        for (MultipartFile archivo : archivosSubidos) {
            if (archivo == null || archivo.isEmpty()) {
                continue;
            }
            String ruta = archivos.guardar(archivo, "oferta-" + idOferta);
            OfertaImagen imagen = new OfertaImagen();
            imagen.setIdOferta(idOferta);
            imagen.setRuta(ruta);
            imagenes.save(imagen);
        }
    }

    private void borrarImagenes(Integer idOferta) {
        for (OfertaImagen imagen : imagenes.findByIdOferta(idOferta)) {
            archivos.borrar(imagen.getRuta());
            imagenes.delete(imagen);
        }
    }
}
