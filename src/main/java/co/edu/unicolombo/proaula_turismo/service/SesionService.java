package co.edu.unicolombo.proaula_turismo.service;

import org.springframework.stereotype.Service;

import co.edu.unicolombo.proaula_turismo.model.Usuario;
import jakarta.servlet.http.HttpSession;

/** Maneja el usuario autenticado y los datos temporales de la reserva. */
@Service
public class SesionService {

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

    public void cerrar(HttpSession sesion) {
        sesion.invalidate();
    }
}
