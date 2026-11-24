package co.edu.unicolombo.proaula_turismo.service;

import org.springframework.stereotype.Service;

import co.edu.unicolombo.proaula_turismo.model.Usuario;
import jakarta.servlet.http.HttpSession;


@Service
public class SesionService {

    // Claves con las que se guardan cosas dentro de la sesion del navegador.
    public static final String USUARIO = "usuarioSesion";
    public static final String ITINERARIO = "idItinerarioActual";
    public static final String RESERVA = "idReservaActual";

    public void iniciar(HttpSession sesion, Usuario usuario) {
        sesion.setAttribute(USUARIO, usuario);
    }

    public Usuario obtener(HttpSession sesion) {
        Object valor = sesion.getAttribute(USUARIO);
        return valor instanceof Usuario ? (Usuario) valor : null;
    }

    public Integer idUsuario(HttpSession sesion) {
        Usuario usuario = obtener(sesion);
        return usuario == null ? null : usuario.getIdUsuario();
    }

    // invalidate() destruye la sesion y borra todo lo guardado en ella.
    public void cerrar(HttpSession sesion) {
        sesion.invalidate();
    }
}
