package co.edu.unicolombo.proaula_turismo.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import co.edu.unicolombo.proaula_turismo.model.Usuario;
import co.edu.unicolombo.proaula_turismo.service.ArchivoService;
import co.edu.unicolombo.proaula_turismo.service.OfertaService;
import co.edu.unicolombo.proaula_turismo.service.SesionService;
import co.edu.unicolombo.proaula_turismo.service.UsuarioService;
import co.edu.unicolombo.proaula_turismo.service.dto.RegistroUsuario;
import jakarta.servlet.http.HttpSession;

@Controller
public class InicioController {

    private final UsuarioService usuarios;
    private final OfertaService ofertas;
    private final ArchivoService archivos;
    private final SesionService sesion;

    public InicioController(UsuarioService usuarios, OfertaService ofertas,
            ArchivoService archivos, SesionService sesion) {
        this.usuarios = usuarios;
        this.ofertas = ofertas;
        this.archivos = archivos;
        this.sesion = sesion;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String correo, @RequestParam String contrasena,
            HttpSession sesionHttp, RedirectAttributes mensajes) {
        return usuarios.autenticar(correo, contrasena).map(usuario -> {
            sesion.iniciar(sesionHttp, usuario);
            return "redirect:" + paginaInicial(usuario);
        }).orElseGet(() -> {
            mensajes.addFlashAttribute("error", "Correo o contrasena incorrectos.");
            return "redirect:/login";
        });
    }

    @GetMapping("/logout")
    public String salir(HttpSession sesionHttp) {
        sesion.cerrar(sesionHttp);
        return "redirect:/login";
    }

    @GetMapping("/catalogo")
    public String catalogo(Model modelo) {
        var publicadas = ofertas.publicadas();
        Map<Integer, String> imagenes = new LinkedHashMap<>();
        for (var oferta : publicadas) {
            var lista = ofertas.imagenes(oferta.getIdOferta());
            if (!lista.isEmpty()) {
                imagenes.put(oferta.getIdOferta(), lista.get(0).getRuta());
            }
        }
        modelo.addAttribute("ofertas", publicadas);
        modelo.addAttribute("imagenesPorOferta", imagenes);
        return "turista/catalogo";
    }

    @GetMapping("/registro-turista")
    public String registroTurista() {
        return "registro-turista";
    }

    @GetMapping("/registro-agencia")
    public String registroAgencia() {
        return "registro-agencia";
    }

    @GetMapping("/registro")
    public String registroPorTipo(@RequestParam(name = "tipo", required = false) String tipo) {
        if ("proveedor".equalsIgnoreCase(tipo)) {
            return "registro-proveedor";
        }
        if ("agencia".equalsIgnoreCase(tipo)) {
            return "registro-agencia";
        }
        return "registro-turista";
    }

    @PostMapping("/registro")
    public String procesarRegistro(
            @RequestParam(name = "nombres", required = false) String nombres,
            @RequestParam(name = "documento", required = false) String documento,
            @RequestParam(name = "telefono", required = false) String telefono,
            @RequestParam(name = "correo", required = false) String correo,
            @RequestParam(name = "contrasena", required = false) String contrasena,
            @RequestParam(name = "tipoUsuario", required = false) String tipoUsuario,
            @RequestParam(name = "nombreNegocio", required = false) String nombreNegocio,
            @RequestParam(name = "tipoServicio", required = false) String tipoServicio,
            @RequestParam(name = "isla", required = false) String isla,
            @RequestParam(name = "descripcion", required = false) String descripcion,
            @RequestParam(name = "razonSocial", required = false) String razonSocial,
            @RequestParam(name = "nit", required = false) String nit,
            @RequestParam(name = "representante", required = false) String representante,
            @RequestParam(name = "cedulaRepresentante", required = false) String cedulaRepresentante,
            @RequestParam(name = "rnt", required = false) MultipartFile rnt,
            RedirectAttributes mensajes) {

        try {
            String rutaRnt = archivos.guardar(rnt, "rnt");
            usuarios.registrar(new RegistroUsuario(nombres, documento, telefono, correo, contrasena,
                    tipoUsuario, nombreNegocio, tipoServicio, isla, descripcion,
                    razonSocial, nit, representante, cedulaRepresentante, rutaRnt));
            mensajes.addFlashAttribute("registroExitoso", "Cuenta creada. Inicia sesion para continuar.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            return "redirect:" + destinoRegistro(tipoUsuario);
        }
    }

    private String destinoRegistro(String tipo) {
        if ("Agencia".equalsIgnoreCase(tipo)) {
            return "/registro-agencia";
        }
        if ("Proveedor".equalsIgnoreCase(tipo)) {
            return "/registro?tipo=proveedor";
        }
        return "/registro-turista";
    }

    private String paginaInicial(Usuario usuario) {
        return switch (usuario.getTipoUsuario()) {
            case "Proveedor" -> "/proveedor/panel";
            case "Agencia" -> "/agencia/panel";
            case "Supervisor" -> "/supervisor/panel";
            default -> "/catalogo";
        };
    }
}
