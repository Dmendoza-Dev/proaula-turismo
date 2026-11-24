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
            "/catalogo", "/logout", "/error");

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
        String ruta = peticion.getRequestURI();
        if (esPublica(ruta)) {
            return true;
        }

        HttpSession sesionHttp = peticion.getSession(false);
        Usuario usuario = sesionHttp == null ? null : sesion.obtener(sesionHttp);
        if (usuario == null) {
            respuesta.sendRedirect("/login");
            return false;
        }

        String tipo = usuario.getTipoUsuario();
        String requerido = ruta.startsWith("/proveedor/") ? "Proveedor"
                : ruta.startsWith("/agencia/") ? "Agencia"
                : ruta.startsWith("/supervisor/") ? "Supervisor"
                : ruta.startsWith("/turista/") ? "Cliente" : null;

        if (requerido != null && !requerido.equals(tipo)) {
            respuesta.sendRedirect(paginaInicial(tipo));
            return false;
        }
        return true;
    }

    private boolean esPublica(String ruta) {
        return PUBLICAS.contains(ruta) || ruta.equals("/login/") || ruta.startsWith("/error");
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
