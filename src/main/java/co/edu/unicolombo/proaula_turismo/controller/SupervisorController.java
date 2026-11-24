package co.edu.unicolombo.proaula_turismo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

import co.edu.unicolombo.proaula_turismo.model.Oferta;
import co.edu.unicolombo.proaula_turismo.repository.UsuarioRepository;
import co.edu.unicolombo.proaula_turismo.service.OfertaService;

@Controller
public class SupervisorController {

    private final OfertaService ofertas;
    private final UsuarioRepository usuarios;

    public SupervisorController(OfertaService ofertas, UsuarioRepository usuarios) {
        this.ofertas = ofertas;
        this.usuarios = usuarios;
    }

    @GetMapping("/supervisor/panel")
    public String panel(Model modelo) {
        var todas = ofertas.todas();
        modelo.addAttribute("pendientes", ofertas.pendientes());
        modelo.addAttribute("publicadas", todas.stream()
                .filter(o -> OfertaService.PUBLICADA.equals(o.getEstadoOferta())).count());
        modelo.addAttribute("rechazadas", todas.stream()
                .filter(o -> OfertaService.RECHAZADA.equals(o.getEstadoOferta())).toList());
        modelo.addAttribute("total", todas.size());
        return "supervisor/panel";
    }

    @GetMapping("/supervisor/lista")
    public String lista(@RequestParam(name = "filtro", required = false, defaultValue = "pendientes") String filtro,
            Model modelo) {
        List<Oferta> lista = "todas".equalsIgnoreCase(filtro) ? ofertas.todas() : ofertas.pendientes();
        modelo.addAttribute("ofertas", lista);
        modelo.addAttribute("filtro", filtro);
        modelo.addAttribute("tipos", usuarios.findAll().stream()
                .collect(Collectors.toMap(u -> u.getIdUsuario(), u -> u.getTipoUsuario(),
                        (a, b) -> a, java.util.LinkedHashMap::new)));
        return "supervisor/lista";
    }

    @PostMapping("/supervisor/aprobar/{id}")
    public String aprobar(@PathVariable Integer id, RedirectAttributes mensajes) {
        try {
            ofertas.aprobar(id);
            mensajes.addFlashAttribute("mensaje", "Oferta aprobada y publicada.");
        } catch (IllegalArgumentException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/supervisor/lista";
    }

    @PostMapping("/supervisor/rechazar/{id}")
    public String rechazar(@PathVariable Integer id, RedirectAttributes mensajes) {
        try {
            ofertas.rechazar(id);
            mensajes.addFlashAttribute("mensaje", "Oferta rechazada.");
        } catch (IllegalArgumentException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/supervisor/lista";
    }
}
