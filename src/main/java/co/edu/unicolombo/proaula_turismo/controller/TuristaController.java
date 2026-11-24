package co.edu.unicolombo.proaula_turismo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import co.edu.unicolombo.proaula_turismo.model.Itinerario;
import co.edu.unicolombo.proaula_turismo.service.FacturaService;
import co.edu.unicolombo.proaula_turismo.service.ItinerarioService;
import co.edu.unicolombo.proaula_turismo.service.ReservaService;
import co.edu.unicolombo.proaula_turismo.service.SesionService;
import co.edu.unicolombo.proaula_turismo.service.dto.ItemCarrito;
import jakarta.servlet.http.HttpSession;

@Controller
public class TuristaController {

    private final ItinerarioService itinerarios;
    private final ReservaService reservas;
    private final FacturaService factura;
    private final SesionService sesion;

    public TuristaController(ItinerarioService itinerarios, ReservaService reservas,
            FacturaService factura, SesionService sesion) {
        this.itinerarios = itinerarios;
        this.reservas = reservas;
        this.factura = factura;
        this.sesion = sesion;
    }

    /** Recibe el carrito del catalogo y crea el itinerario en la BD. */
    @PostMapping("/turista/reserva")
    public String crearItinerario(
            @RequestParam(name = "idOferta", required = false) List<Integer> ids,
            @RequestParam(name = "cantidad", required = false) List<Integer> cantidades,
            HttpSession http, RedirectAttributes mensajes) {
        try {
            List<ItemCarrito> items = new ArrayList<>();
            if (ids != null && cantidades != null) {
                if (ids.size() != cantidades.size()) {
                    throw new IllegalArgumentException("El carrito no es valido.");
                }
                for (int i = 0; i < ids.size(); i++) {
                    items.add(new ItemCarrito(ids.get(i), cantidades.get(i)));
                }
            }
            Itinerario itinerario = itinerarios.crear(items, sesion.idUsuario(http));
            http.setAttribute(SesionService.ITINERARIO, itinerario.getIdItinerario());
            return "redirect:/turista/reserva";
        } catch (IllegalArgumentException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            return "redirect:/catalogo";
        }
    }

    @GetMapping("/turista/reserva")
    public String reserva(HttpSession http, Model modelo, RedirectAttributes mensajes) {
        Integer id = (Integer) http.getAttribute(SesionService.ITINERARIO);
        if (id == null) {
            mensajes.addFlashAttribute("error", "Primero arma tu itinerario en el catalogo.");
            return "redirect:/catalogo";
        }
        try {
            modelo.addAttribute("itinerario", itinerarios.obtener(id, sesion.idUsuario(http)));
            modelo.addAttribute("items", itinerarios.items(id));
            return "turista/reserva";
        } catch (IllegalArgumentException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            return "redirect:/catalogo";
        }
    }

    @PostMapping("/turista/pagar")
    public String pagar(@RequestParam(name = "metodoPago") String metodoPago,
            HttpSession http, RedirectAttributes mensajes) {
        Integer id = (Integer) http.getAttribute(SesionService.ITINERARIO);
        if (id == null) {
            mensajes.addFlashAttribute("error", "No hay un itinerario para pagar.");
            return "redirect:/catalogo";
        }
        try {
            var reserva = reservas.pagar(id, metodoPago, sesion.idUsuario(http));
            http.setAttribute(SesionService.RESERVA, reserva.getIdReserva());
            // Solo esta visita a la factura debe limpiar el carrito del navegador.
            mensajes.addFlashAttribute("pagoExitoso", true);
            return "redirect:/turista/factura";
        } catch (IllegalArgumentException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            return "redirect:/turista/reserva";
        }
    }

    @GetMapping("/turista/factura")
    public String factura(HttpSession http, Model modelo, RedirectAttributes mensajes) {
        Integer id = (Integer) http.getAttribute(SesionService.RESERVA);
        if (id == null) {
            mensajes.addFlashAttribute("error", "Aun no tienes una reserva confirmada.");
            return "redirect:/catalogo";
        }
        try {
            modelo.addAttribute("factura", factura.construir(id, sesion.idUsuario(http)));
            return "turista/factura";
        } catch (IllegalArgumentException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            return "redirect:/catalogo";
        }
    }
}
