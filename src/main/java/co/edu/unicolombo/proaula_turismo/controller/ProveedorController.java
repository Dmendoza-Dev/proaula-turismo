package co.edu.unicolombo.proaula_turismo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import co.edu.unicolombo.proaula_turismo.model.Oferta;
import co.edu.unicolombo.proaula_turismo.service.OfertaFormParser;
import co.edu.unicolombo.proaula_turismo.service.OfertaService;
import co.edu.unicolombo.proaula_turismo.service.SesionService;
import jakarta.servlet.http.HttpSession;

@Controller
public class ProveedorController {

    private final OfertaService ofertas;
    private final OfertaFormParser parser;
    private final SesionService sesion;

    public ProveedorController(OfertaService ofertas, OfertaFormParser parser, SesionService sesion) {
        this.ofertas = ofertas;
        this.parser = parser;
        this.sesion = sesion;
    }

    @GetMapping("/proveedor/panel")
    public String panel(Model modelo, HttpSession http) {
        // Consulta a la BD: trae solo las ofertas del publicador que esta logueado.
        List<Oferta> propias = ofertas.porPublicador(sesion.idUsuario(http));
        modelo.addAttribute("total", propias.size());
        // count() cuenta cuantas ofertas hay en cada estado para el panel.
        modelo.addAttribute("pendientes", propias.stream()
                .filter(o -> OfertaService.PENDIENTE.equals(o.getEstadoOferta())).count());
        modelo.addAttribute("publicadas", propias.stream()
                .filter(o -> OfertaService.PUBLICADA.equals(o.getEstadoOferta())).count());
        modelo.addAttribute("rechazadas", propias.stream()
                .filter(o -> OfertaService.RECHAZADA.equals(o.getEstadoOferta())).count());
        modelo.addAttribute("canceladas", propias.stream()
                .filter(o -> OfertaService.CANCELADA.equals(o.getEstadoOferta())).count());
        modelo.addAttribute("recientes", propias.stream().limit(4).toList());
        return "proveedor/panel";
    }

    @GetMapping("/proveedor/nueva")
    public String nueva(Model modelo) {
        modelo.addAttribute("oferta", new Oferta());
        return "proveedor/nueva";
    }

    @GetMapping("/proveedor/lista")
    public String lista(Model modelo, HttpSession http) {
        modelo.addAttribute("ofertas", ofertas.porPublicador(sesion.idUsuario(http)));
        return "proveedor/lista";
    }

    @GetMapping("/proveedor/editar")
    public String editar(@RequestParam(name = "id", required = false) Integer id,
            Model modelo, RedirectAttributes mensajes, HttpSession http) {
        try {
            // Si no viene el id, requerido lanza un error en vez de seguir.
            Oferta oferta = ofertas.buscar(requerido(id));
            // Otra vez la regla: nadie edita ofertas ajenas.
            if (!oferta.getIdProveedor().equals(sesion.idUsuario(http))) {
                mensajes.addFlashAttribute("error", "No puedes editar esa oferta.");
                return "redirect:/proveedor/lista";
            }
            modelo.addAttribute("oferta", oferta);
            modelo.addAttribute("imagenes", ofertas.imagenes(oferta.getIdOferta()));
            return "proveedor/editar";
        // El mensaje de la validacion se muestra al usuario, no se lanza un 500.
        } catch (IllegalArgumentException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            return "redirect:/proveedor/lista";
        }
    }

    @GetMapping("/proveedor/eliminar")
    public String eliminar(@RequestParam(name = "id", required = false) Integer id,
            Model modelo, RedirectAttributes mensajes, HttpSession http) {
        try {
            Oferta oferta = ofertas.buscar(requerido(id));
            if (!oferta.getIdProveedor().equals(sesion.idUsuario(http))) {
                mensajes.addFlashAttribute("error", "No puedes eliminar esa oferta.");
                return "redirect:/proveedor/lista";
            }
            modelo.addAttribute("oferta", oferta);
            return "proveedor/eliminar";
        } catch (IllegalArgumentException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            return "redirect:/proveedor/lista";
        }
    }

    @PostMapping("/proveedor/lista")
    public String guardar(
            @RequestParam(name = "categoria") String categoria,
            @RequestParam(name = "nombre") String nombre,
            @RequestParam(name = "descripcion", required = false) String descripcion,
            @RequestParam(name = "isla") String isla,
            @RequestParam(name = "tarifa") String tarifa,
            @RequestParam(name = "cupos") String cupos,
            @RequestParam(name = "dias", required = false) List<String> dias,
            @RequestParam(name = "horarioInicio", required = false) String horarioInicio,
            @RequestParam(name = "horarioFin", required = false) String horarioFin,
            @RequestParam(name = "imagenes", required = false) List<MultipartFile> imagenes,
            RedirectAttributes mensajes, HttpSession http) {
        try {
            // parser convierte el texto del formulario en numeros antes de validar.
            ofertas.crear(parser.parsear(categoria, nombre, descripcion, isla, tarifa, cupos,
                    dias, horarioInicio, horarioFin, imagenes), sesion.idUsuario(http));
            mensajes.addFlashAttribute("mensaje", "Oferta enviada a auditoria.");
        } catch (IllegalArgumentException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/proveedor/lista";
    }

    @PostMapping("/proveedor/actualizar")
    public String actualizar(
            @RequestParam(name = "idOferta") Integer idOferta,
            @RequestParam(name = "categoria") String categoria,
            @RequestParam(name = "nombre") String nombre,
            @RequestParam(name = "descripcion", required = false) String descripcion,
            @RequestParam(name = "isla") String isla,
            @RequestParam(name = "tarifa") String tarifa,
            @RequestParam(name = "cupos") String cupos,
            @RequestParam(name = "dias", required = false) List<String> dias,
            @RequestParam(name = "horarioInicio", required = false) String horarioInicio,
            @RequestParam(name = "horarioFin", required = false) String horarioFin,
            @RequestParam(name = "imagenes", required = false) List<MultipartFile> imagenes,
            RedirectAttributes mensajes, HttpSession http) {
        try {
            ofertas.actualizar(idOferta, parser.parsear(categoria, nombre, descripcion, isla,
                    tarifa, cupos, dias, horarioInicio, horarioFin, imagenes), sesion.idUsuario(http));
            mensajes.addFlashAttribute("mensaje", "Oferta actualizada y enviada a auditoria.");
        } catch (IllegalArgumentException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/proveedor/lista";
    }

    @PostMapping("/proveedor/eliminar")
    public String confirmarEliminacion(@RequestParam(name = "idOferta") Integer idOferta,
            RedirectAttributes mensajes, HttpSession http) {
        try {
            ofertas.eliminar(idOferta, sesion.idUsuario(http));
            mensajes.addFlashAttribute("mensaje", "Oferta eliminada.");
        } catch (IllegalArgumentException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/proveedor/lista";
    }

    // El id viene opcional en la URL; aqui lo hacemos obligatorio.
    private Integer requerido(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Selecciona una oferta.");
        }
        return id;
    }
}
