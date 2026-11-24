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

/** Protege las rutas de cada rol y evita que un rol entre en las paginas de otro. */
@Component
public class WebConfig implements WebMvcConfigurer, HandlerInterceptor {

    private static final Set<String> PUBLICAS = Set.of(
            "/", "/index", "/login", "/registro", "/registro-turista", "/registro-agencia",
            "/catalogo", "/logout");

    private final SesionService sesion;

    public WebConfig(SesionService sesion) {
        this.sesion = sesion;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/img/**", "/uploads/**", "/webjars/**");
    }

    @Override
    public boolean preHandle(HttpServletRequest peticion, HttpServletResponse respuesta, Object manejador)
            throws IOException {
        // Spring puede agregar ;jsessionid a las URLs cuando el navegador aun no
        // tiene la cookie. Se ignora ese parametro para que la ruta siga siendo publica.
        String ruta = normalizar(peticion.getRequestURI());
        if (esPublica(ruta)) {
            return true;
        }

        String requerido = rolRequerido(ruta);
        if (requerido == null) {
            // Ruta desconocida: se deja pasar para que Spring responda 404.
            return true;
        }

        HttpSession sesionHttp = peticion.getSession(false);
        Usuario usuario = sesionHttp == null ? null : sesion.obtener(sesionHttp);
        if (usuario == null) {
            respuesta.sendRedirect("/login");
            return false;
        }

        String tipo = usuario.getTipoUsuario();
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
        int parametro = ruta.indexOf(';');
        if (parametro >= 0) {
            ruta = ruta.substring(0, parametro);
        }
        while (ruta.length() > 1 && ruta.endsWith("/")) {
            ruta = ruta.substring(0, ruta.length() - 1);
        }
        return ruta;
    }

    private boolean esPublica(String ruta) {
        return PUBLICAS.contains(ruta) || ruta.startsWith("/error");
    }

    /** Solo los espacios de cada rol exigen sesion; el resto lo resuelve Spring. */
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

    private String paginaInicial(String tipo) {
        return switch (tipo) {
            case "Proveedor" -> "/proveedor/panel";
            case "Agencia" -> "/agencia/panel";
            case "Supervisor" -> "/supervisor/panel";
            default -> "/catalogo";
        };
    }
}
