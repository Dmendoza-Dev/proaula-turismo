package co.edu.unicolombo.proaula_turismo.config;

import java.io.IOException;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import co.edu.unicolombo.proaula_turismo.model.Usuario;
import co.edu.unicolombo.proaula_turismo.service.SesionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


@Component
public class WebConfig implements WebMvcConfigurer, HandlerInterceptor {

    // Estas rutas se pueden abrir sin haber iniciado sesion.
    private static final Set<String> PUBLICAS = Set.of(
            "/", "/index", "/login", "/registro", "/registro-turista", "/registro-agencia",
            "/catalogo", "/logout");

    private final SesionService sesion;

    public WebConfig(SesionService sesion) {
        this.sesion = sesion;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // El filtro se aplica casi a toda la web, menos a imagenes y archivos.
        registry.addInterceptor(this)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/img/**", "/uploads/**", "/webjars/**");
    }

    @Override
    public boolean preHandle(HttpServletRequest peticion, HttpServletResponse respuesta, Object manejador)
            throws IOException {
        
        
        // Quita el ;jsessionid y la barra final para comparar rutas iguales.
        String ruta = normalizar(peticion.getRequestURI());
        // Si la pagina es publica, se deja pasar sin revisar el rol.
        if (esPublica(ruta)) {
            return true;
        }

        // Segun la carpeta de la ruta, decide que rol se necesita.
        String requerido = rolRequerido(ruta);
        if (requerido == null) {
            
            return true;
        }

        // false = no crear sesion nueva solo por mirar; si no hay, usuario queda null.
        HttpSession sesionHttp = peticion.getSession(false);
        Usuario usuario = sesionHttp == null ? null : sesion.obtener(sesionHttp);
        // Sin sesion valida se manda al login.
        if (usuario == null) {
            respuesta.sendRedirect("/login");
            return false;
        }

        String tipo = usuario.getTipoUsuario();
        // Si el rol no coincide, se envia a su propia pagina inicial.
        if (!requerido.equals(tipo)) {
            respuesta.sendRedirect(paginaInicial(tipo));
            return false;
        }
        return true;
    }

    private String normalizar(String ruta) {
        if (ruta == null || ruta.isEmpty()) {
            return "/";
        }
        // El ;jsessionid va pegado a la URL y no debe contar como parte de la ruta.
        int parametro = ruta.indexOf(';');
        if (parametro >= 0) {
            ruta = ruta.substring(0, parametro);
        }
        // /catalogo/ y /catalogo se tratan como la misma ruta.
        while (ruta.length() > 1 && ruta.endsWith("/")) {
            ruta = ruta.substring(0, ruta.length() - 1);
        }
        return ruta;
    }

    private boolean esPublica(String ruta) {
        return PUBLICAS.contains(ruta) || ruta.startsWith("/error");
    }

    
    // Cada carpeta delante de la ruta exige un tipo de usuario distinto.
    private String rolRequerido(String ruta) {
        if (ruta.startsWith("/proveedor/")) {
            return "Proveedor";
        }
        if (ruta.startsWith("/agencia/")) {
            return "Agencia";
        }
        if (ruta.startsWith("/supervisor/")) {
            return "Supervisor";
        }
        if (ruta.startsWith("/turista/")) {
            return "Cliente";
        }
        return null;
    }

    // A cada rol se le abre su panel; el turista cae en el catalogo.
    private String paginaInicial(String tipo) {
        return switch (tipo) {
            case "Proveedor" -> "/proveedor/panel";
            case "Agencia" -> "/agencia/panel";
            case "Supervisor" -> "/supervisor/panel";
            default -> "/catalogo";
        };
    }
}
