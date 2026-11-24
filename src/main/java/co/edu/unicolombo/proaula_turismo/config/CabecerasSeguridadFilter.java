package co.edu.unicolombo.proaula_turismo.config;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;





@Component
public class CabecerasSeguridadFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta,
            FilterChain cadena) throws ServletException, IOException {
        // Evita que el navegador adivine el tipo de archivo y ejecute algo raro.
        respuesta.setHeader("X-Content-Type-Options", "nosniff");
        // Evita que otra pagina metaa este sitio dentro de un marco.
        respuesta.setHeader("X-Frame-Options", "SAMEORIGIN");
        cadena.doFilter(peticion, respuesta);
    }

    @Override
    // true seria saltarse el filtro en paginas de error; aqui se aplica tambien.
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }
}
