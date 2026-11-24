package co.edu.unicolombo.proaula_turismo.config;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Cabeceras minimas para el prototipo. No se usa Spring Security.
 * No se agregan restricciones que puedan romper la navegacion normal.
 */
@Component
public class CabecerasSeguridadFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta,
            FilterChain cadena) throws ServletException, IOException {
        respuesta.setHeader("X-Content-Type-Options", "nosniff");
        respuesta.setHeader("X-Frame-Options", "SAMEORIGIN");
        cadena.doFilter(peticion, respuesta);
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }
}
