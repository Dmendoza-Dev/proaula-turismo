package co.edu.unicolombo.proaula_turismo.service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
        OfertaForm validos = ReglasOferta.normalizarYValidar(datos);
        Oferta oferta = new Oferta();
        aplicar(validos, oferta);
        oferta.setEstadoOferta(PENDIENTE);
        oferta.setIdProveedor(idUsuario);
        ofertas.save(oferta);
        guardarImagenes(validos.imagenes(), oferta.getIdOferta());
        return oferta;
    }

    @Transactional
    public Oferta actualizar(Integer idOferta, OfertaForm datos, Integer idUsuario) {
        Oferta oferta = ofertas.findById(idOferta)
                .orElseThrow(() -> new IllegalArgumentException("La oferta no existe."));
        if (!oferta.getIdProveedor().equals(idUsuario)) {
            throw new IllegalArgumentException("No puedes editar una oferta de otro publicador.");
        }
        OfertaForm validos = ReglasOferta.normalizarYValidar(datos);
        aplicar(validos, oferta);
        // Toda modificacion vuelve a auditoria.
        oferta.setEstadoOferta(PENDIENTE);
        ofertas.save(oferta);
        if (tieneImagenes(validos.imagenes())) {
            borrarImagenes(idOferta);
            guardarImagenes(validos.imagenes(), idOferta);
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
        if (detalles.existsByIdOferta(idOferta)) {
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

    /** Primera imagen de cada oferta en una sola consulta (evita N+1 en el catalogo). */
    public Map<Integer, String> imagenesPrincipales(List<Oferta> lista) {
        Map<Integer, String> resultado = new LinkedHashMap<>();
        if (lista == null || lista.isEmpty()) {
            return resultado;
        }
        List<Integer> ids = lista.stream().map(Oferta::getIdOferta).toList();
        for (OfertaImagen imagen : imagenes.findByIdOfertaIn(ids)) {
            resultado.putIfAbsent(imagen.getIdOferta(), imagen.getRuta());
        }
        return resultado;
    }

    private void aplicar(OfertaForm datos, Oferta oferta) {
        oferta.setNombreOferta(datos.nombre());
        oferta.setDescripcion(datos.descripcion());
        oferta.setCategoria(datos.categoria());
        oferta.setIsla(datos.isla());
        oferta.setPrecio(datos.tarifa());
        oferta.setCuposMaximos(datos.cupos());
        oferta.setDiasDisponibles(datos.dias() == null || datos.dias().isEmpty()
                ? null : String.join(", ", datos.dias()));
        oferta.setHorarioInicio(hora(datos.horarioInicio()));
        oferta.setHorarioFin(hora(datos.horarioFin()));
    }

    private LocalTime hora(String valor) {
        return ReglasOferta.hora(valor);
    }

    private boolean tieneImagenes(List<MultipartFile> subidos) {
        return subidos != null && subidos.stream().anyMatch(a -> a != null && !a.isEmpty());
    }

    private void guardarImagenes(List<MultipartFile> subidos, Integer idOferta) {
        if (subidos == null) {
            return;
        }
        List<String> rutasGuardadas = new ArrayList<>();
        try {
            for (MultipartFile archivo : subidos) {
                if (archivo == null || archivo.isEmpty()) {
                    continue;
                }
                String ruta = archivos.guardar(archivo, "oferta-" + idOferta);
                if (ruta == null) {
                    continue;
                }
                rutasGuardadas.add(ruta);
                OfertaImagen imagen = new OfertaImagen();
                imagen.setIdOferta(idOferta);
                imagen.setRuta(ruta);
                imagenes.save(imagen);
            }
        } catch (RuntimeException e) {
            // Si la transaccion falla, no se dejan archivos huerfanos en uploads/.
            rutasGuardadas.forEach(archivos::borrar);
            throw e;
        }
    }

    private void borrarImagenes(Integer idOferta) {
        for (OfertaImagen imagen : imagenes.findByIdOferta(idOferta)) {
            archivos.borrar(imagen.getRuta());
            imagenes.delete(imagen);
        }
    }
}
