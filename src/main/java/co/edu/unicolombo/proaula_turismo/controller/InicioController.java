package co.edu.unicolombo.proaula_turismo.controller;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import co.edu.unicolombo.proaula_turismo.model.Oferta;
import co.edu.unicolombo.proaula_turismo.model.Usuario;
import co.edu.unicolombo.proaula_turismo.service.ArchivoService;
import co.edu.unicolombo.proaula_turismo.service.OfertaService;
import co.edu.unicolombo.proaula_turismo.service.SesionService;
import co.edu.unicolombo.proaula_turismo.service.UsuarioService;
import co.edu.unicolombo.proaula_turismo.service.dto.RegistroUsuario;
import jakarta.servlet.http.HttpServletRequest;
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
            HttpServletRequest peticion, HttpSession sesionHttp, RedirectAttributes mensajes) {
        // Si el correo y la clave coinciden, preparamos la entrada.
        return usuarios.autenticar(correo, contrasena).map(usuario -> {
            
            // Cambiamos el id de sesion para que otro no pueda reutilizar el anterior.
            peticion.changeSessionId();
            // Al entrar borramos el carrito viejo que pudiera quedar de otra cuenta.
            sesionHttp.removeAttribute(SesionService.ITINERARIO);
            sesionHttp.removeAttribute(SesionService.RESERVA);
            sesion.iniciar(sesionHttp, usuario);
            return "redirect:" + paginaInicial(usuario);
        }).orElseGet(() -> {
            // Flash = mensaje que se muestra solo en la siguiente pagina.
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
    public String catalogo(@RequestParam(name = "q", required = false) String busqueda, Model modelo) {
        // Traemos de la BD solo las ofertas aprobadas por el supervisor.
        List<Oferta> publicadas = ofertas.publicadas();
        String filtro = normalizar(busqueda);
        // Si el usuario escribio algo en el buscador, filtramos esa lista.
        if (!filtro.isEmpty()) {
            publicadas = publicadas.stream()
                    .filter(oferta -> coincide(oferta, filtro))
                    .toList();
        }
        modelo.addAttribute("ofertas", publicadas);
        // Guardamos la primera foto de cada oferta para no buscarla una por una en la plantilla.
        modelo.addAttribute("imagenesPorOferta", ofertas.imagenesPrincipales(publicadas));
        modelo.addAttribute("q", busqueda == null ? "" : busqueda.trim());
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
        // El parametro ?tipo= decide cual de los tres formularios de registro se abre.
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
            @RequestParam(name = "tipoTurista", required = false) String tipoTurista,
            @RequestParam(name = "nacionalidad", required = false) String nacionalidad,
            @RequestParam(name = "rnt", required = false) MultipartFile rnt,
            RedirectAttributes mensajes) {

        String rutaRnt = null;
        try {
            // Guardamos el PDF del RNT primero; si falla el registro se borra mas abajo.
            rutaRnt = archivos.guardar(rnt, "rnt");
            usuarios.registrar(new RegistroUsuario(nombres, documento, telefono, correo, contrasena,
                    tipoUsuario, nombreNegocio, tipoServicio, isla, descripcion,
                    razonSocial, nit, representante, cedulaRepresentante, rutaRnt,
                    tipoTurista, nacionalidad));
            mensajes.addFlashAttribute("registroExitoso", "Cuenta creada. Inicia sesion para continuar.");
            return "redirect:/login";
        // Validacion del negocio fallida: se borra el PDF subido para no dejar basura.
        } catch (IllegalArgumentException e) {
            archivos.borrar(rutaRnt);
            mensajes.addFlashAttribute("error", e.getMessage());
        // La BD rechazo el registro porque el correo, documento o NIT ya existe.
        } catch (DataIntegrityViolationException e) {
            
            
            archivos.borrar(rutaRnt);
            mensajes.addFlashAttribute("error", "Ya existe una cuenta o agencia con esos datos.");
        // No se pudo escribir el certificado en disco; se limpia lo que se hubiera guardado.
        } catch (IllegalStateException e) {
            archivos.borrar(rutaRnt);
            mensajes.addFlashAttribute("error", "No se pudo guardar el certificado RNT. Intenta de nuevo.");
        }
        return "redirect:" + destinoRegistro(tipoUsuario);
    }

    private boolean coincide(Oferta oferta, String filtro) {
        return contiene(oferta.getNombreOferta(), filtro)
                || contiene(oferta.getCategoria(), filtro)
                || contiene(oferta.getIsla(), filtro)
                || contiene(oferta.getDescripcion(), filtro);
    }

    private boolean contiene(String valor, String filtro) {
        return valor != null && normalizar(valor).contains(filtro);
    }

    // Quita acentos y mayusculas para que Baru y baru busquen igual.
    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        return Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
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
