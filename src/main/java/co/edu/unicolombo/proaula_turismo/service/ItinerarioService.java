package co.edu.unicolombo.proaula_turismo.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicolombo.proaula_turismo.model.DetalleItinerario;
import co.edu.unicolombo.proaula_turismo.model.Itinerario;
import co.edu.unicolombo.proaula_turismo.model.Oferta;
import co.edu.unicolombo.proaula_turismo.repository.DetalleItinerarioRepository;
import co.edu.unicolombo.proaula_turismo.repository.ItinerarioRepository;
import co.edu.unicolombo.proaula_turismo.repository.OfertaRepository;
import co.edu.unicolombo.proaula_turismo.service.dto.ItemCarrito;
import co.edu.unicolombo.proaula_turismo.service.dto.ItemItinerario;

@Service
public class ItinerarioService {

    public static final String PENDIENTE_PAGO = "Pendiente de pago";
    public static final String RESERVADO = "Reservado";
    private static final int DIAS_BUSQUEDA = 90;

    private final ItinerarioRepository itinerarios;
    private final DetalleItinerarioRepository detalles;
    private final OfertaRepository ofertas;

    public ItinerarioService(ItinerarioRepository itinerarios,
            DetalleItinerarioRepository detalles, OfertaRepository ofertas) {
        this.itinerarios = itinerarios;
        this.detalles = detalles;
        this.ofertas = ofertas;
    }

    
    @Transactional
    public Itinerario crear(List<ItemCarrito> items, Integer idCliente) {
        // No se puede armar un itinerario sin ofertas en el carrito.
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Tu itinerario esta vacio.");
        }
        if (idCliente == null) {
            throw new IllegalArgumentException("Tu sesion ya no es valida. Inicia sesion de nuevo.");
        }

        Map<Integer, Oferta> seleccionadas = new LinkedHashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        int personas = 0;
        Set<String> destinos = new LinkedHashSet<>();

        for (ItemCarrito item : items) {
            // Cada linea del carrito debe traer id y cantidad mayor a cero.
            if (item.idOferta() == null || item.cantidad() == null || item.cantidad() < 1) {
                throw new IllegalArgumentException("Hay una cantidad no valida en el carrito.");
            }
            // Consulta a la BD: la oferta pudo borrarse mientras el turista navegaba.
            Oferta oferta = ofertas.findById(item.idOferta())
                    .orElseThrow(() -> new IllegalArgumentException("Una oferta del carrito ya no existe."));
            // No se pueden meter ofertas que no esten aprobadas.
            if (!OfertaService.PUBLICADA.equals(oferta.getEstadoOferta())) {
                throw new IllegalArgumentException("La oferta " + oferta.getNombreOferta() + " no esta disponible.");
            }
            // No dejamos reservar mas plazas de las que hay.
            if (oferta.getCuposMaximos() < item.cantidad()) {
                throw new IllegalArgumentException("No hay cupos suficientes para " + oferta.getNombreOferta() + ".");
            }
            // La misma oferta repetida se sumaria dos veces al precio.
            if (seleccionadas.containsKey(item.idOferta())) {
                throw new IllegalArgumentException("Hay ofertas repetidas en el carrito.");
            }
            seleccionadas.put(item.idOferta(), oferta);
            // Precio por unidad por cantidad: asi se arma el total.
            total = total.add(oferta.getPrecio().multiply(BigDecimal.valueOf(item.cantidad())));
            // El numero de personas es la mayor cantidad pedida en una sola oferta.
            personas = Math.max(personas, item.cantidad());
            destinos.add(oferta.getIsla());
        }

        
        // Busca el primer dia en que TODAS las ofertas estan disponibles.
        LocalDate fechaVisita = DisponibilidadOfertas.primeraFechaCompatible(
                seleccionadas.values(), LocalDate.now().plusDays(1), DIAS_BUSQUEDA);
        // Si no hay dia comun (por ejemplo Lunes y Martes), se rechaza el carrito.
        if (fechaVisita == null) {
            throw new IllegalArgumentException(
                    "Las ofertas elegidas no coinciden en ningun dia disponible en los proximos "
                            + DIAS_BUSQUEDA + " dias. Quita una oferta o elige otras.");
        }

        Itinerario itinerario = new Itinerario();
        itinerario.setNombreItinerario("Itinerario " + LocalDate.now());
        // Varias islas se juntan con y; se recorta para que quepa en la BD.
        itinerario.setDestino(recortar(String.join(" y ", destinos), 150));
        itinerario.setFechaVisita(fechaVisita);
        itinerario.setNumPersonas(personas);
        itinerario.setCostoTotal(total);
        // El itinerario nace sin pagar; cambia a Reservado cuando se pague.
        itinerario.setEstadoItinerario(PENDIENTE_PAGO);
        itinerario.setIdCliente(idCliente);
        itinerarios.save(itinerario);

        for (ItemCarrito item : items) {
            Oferta oferta = seleccionadas.get(item.idOferta());
            DetalleItinerario detalle = new DetalleItinerario();
            detalle.setCantidad(item.cantidad());
            detalle.setPrecioUnitario(oferta.getPrecio());
            detalle.setSubtotal(oferta.getPrecio().multiply(BigDecimal.valueOf(item.cantidad())));
            detalle.setIdItinerario(itinerario.getIdItinerario());
            detalle.setIdOferta(item.idOferta());
            detalles.save(detalle);
        }
        return itinerario;
    }

    public Itinerario obtener(Integer idItinerario, Integer idCliente) {
        Itinerario itinerario = itinerarios.findById(idItinerario)
                .orElseThrow(() -> new IllegalArgumentException("El itinerario no existe."));
        // Un turista no puede abrir el itinerario de otro.
        if (!itinerario.getIdCliente().equals(idCliente)) {
            throw new IllegalArgumentException("Ese itinerario no te pertenece.");
        }
        return itinerario;
    }

    public List<ItemItinerario> items(Integer idItinerario) {
        List<DetalleItinerario> lista = detalles.findByIdItinerario(idItinerario);
        if (lista.isEmpty()) {
            return List.of();
        }
        // Juntamos los ids unicos para consultar todas las ofertas de una vez.
        List<Integer> ids = lista.stream().map(DetalleItinerario::getIdOferta).distinct().toList();
        Map<Integer, Oferta> porId = ofertas.findAllById(ids).stream()
                .collect(Collectors.toMap(Oferta::getIdOferta, Function.identity()));

        List<ItemItinerario> resultado = new ArrayList<>();
        for (DetalleItinerario detalle : lista) {
            Oferta oferta = porId.get(detalle.getIdOferta());
            if (oferta == null) {
                throw new IllegalArgumentException("Una oferta del itinerario no existe.");
            }
            resultado.add(new ItemItinerario(
                    oferta.getNombreOferta(), oferta.getCategoria(), oferta.getIsla(),
                    detalle.getCantidad(), detalle.getPrecioUnitario(), detalle.getSubtotal()));
        }
        return resultado;
    }

    




    @Transactional
    // Si un cupo falla a mitad, se deshacen los descuentos ya hechos.
    public void descontarCupos(Integer idItinerario) {
        Itinerario itinerario = itinerarios.findById(idItinerario)
                .orElseThrow(() -> new IllegalArgumentException("El itinerario no existe."));
        for (DetalleItinerario detalle : detalles.findByIdItinerario(idItinerario)) {
            Oferta oferta = ofertas.findById(detalle.getIdOferta())
                    .orElseThrow(() -> new IllegalArgumentException("Una oferta del itinerario no existe."));
            if (!OfertaService.PUBLICADA.equals(oferta.getEstadoOferta())) {
                throw new IllegalArgumentException("La oferta " + oferta.getNombreOferta() + " ya no esta disponible.");
            }
            // Se vuelve a revisar el dia porque el itinerario ya tiene fecha fija.
            if (!DisponibilidadOfertas.disponibleEn(oferta, itinerario.getFechaVisita())) {
                throw new IllegalArgumentException("La oferta " + oferta.getNombreOferta()
                        + " ya no se ofrece en la fecha del itinerario.");
            }
            // Ultima revision de cupos justo antes de pagar.
            if (oferta.getCuposMaximos() < detalle.getCantidad()) {
                throw new IllegalArgumentException("Ya no hay cupos suficientes para " + oferta.getNombreOferta() + ".");
            }
            // Restamos las plazas vendidas de la oferta en la BD.
            oferta.setCuposMaximos(oferta.getCuposMaximos() - detalle.getCantidad());
            ofertas.save(oferta);
        }
    }

    private String recortar(String texto, int limite) {
        return texto.length() <= limite ? texto : texto.substring(0, limite);
    }
}
