package co.edu.unicolombo.proaula_turismo.service;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import co.edu.unicolombo.proaula_turismo.service.dto.RegistroUsuario;

/**
 * Reglas de validacion compartidas por los tres formularios de registro.
 * Se mantienen fuera del controlador para poder probarlas sin levantar Spring.
 */
public final class ReglasRegistro {

    public static final Set<String> ISLAS = Set.of("Tierra Bomba", "Barú", "Islas del Rosario");

    public static final Set<String> TIPOS_SERVICIO = Set.of(
            "Alimentación", "Carpas y playa", "Deportes acuáticos", "Transporte",
            "Hospedaje", "Artesanías", "Guía turístico", "Otro servicio");

    private static final Pattern CORREO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");

    private ReglasRegistro() {
    }

    public static String normalizarTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return "Cliente";
        }
        return switch (tipo.trim().toLowerCase(Locale.ROOT)) {
            case "proveedor" -> "Proveedor";
            case "agencia" -> "Agencia";
            case "cliente", "turista" -> "Cliente";
            case "supervisor" -> throw new IllegalArgumentException("El registro de supervisores no esta habilitado.");
            default -> throw new IllegalArgumentException("Tipo de usuario no valido.");
        };
    }

    public static String normalizarTipoTurista(String valor) {
        return valor != null && "residente".equalsIgnoreCase(valor.trim()) ? "Residente" : "Turista";
    }

    /**
     * Recorta los textos y completa el nombre/documento de la agencia con los
     * datos del representante, que es lo que envia su formulario.
     */
    public static RegistroUsuario normalizar(RegistroUsuario datos, String tipo) {
        String nombres = texto(datos.nombres());
        String documento = texto(datos.documento());
        if ("Agencia".equals(tipo)) {
            if (nombres == null) {
                nombres = texto(datos.representante());
            }
            if (documento == null) {
                documento = texto(datos.cedulaRepresentante());
            }
        }
        String correo = texto(datos.correo());
        String nacionalidad = texto(datos.nacionalidad());
        return new RegistroUsuario(
                nombres,
                documento,
                texto(datos.telefono()),
                correo == null ? null : correo.toLowerCase(Locale.ROOT),
                datos.contrasena(),
                tipo,
                texto(datos.nombreNegocio()),
                texto(datos.tipoServicio()),
                texto(datos.isla()),
                texto(datos.descripcion()),
                texto(datos.razonSocial()),
                texto(datos.nit()),
                texto(datos.representante()),
                texto(datos.cedulaRepresentante()),
                texto(datos.rutaRnt()),
                normalizarTipoTurista(datos.tipoTurista()),
                nacionalidad == null ? "Colombiana" : nacionalidad);
    }

    public static void validar(RegistroUsuario datos, String tipo) {
        requerido(datos.nombres(), "El nombre es obligatorio.", 100);
        requerido(datos.documento(), "El documento es obligatorio.", 20);
        requerido(datos.correo(), "El correo es obligatorio.", 100);
        if (!CORREO.matcher(datos.correo()).matches()) {
            throw new IllegalArgumentException("El correo no tiene un formato valido.");
        }
        if (datos.contrasena() == null
                || datos.contrasena().length() < 8
                || datos.contrasena().length() > 72) {
            throw new IllegalArgumentException("La contrasena debe tener entre 8 y 72 caracteres.");
        }
        opcional(datos.telefono(), "El telefono no puede superar 20 caracteres.", 20);
        opcional(datos.nacionalidad(), "La nacionalidad no puede superar 60 caracteres.", 60);

        if ("Proveedor".equals(tipo)) {
            requerido(datos.nombreNegocio(), "El nombre del negocio es obligatorio.", 100);
            requerido(datos.tipoServicio(), "El tipo de servicio es obligatorio.", 50);
            requerido(datos.isla(), "La isla es obligatoria.", 100);
            if (!TIPOS_SERVICIO.contains(datos.tipoServicio())) {
                throw new IllegalArgumentException("Selecciona un tipo de servicio valido.");
            }
            if (!ISLAS.contains(datos.isla())) {
                throw new IllegalArgumentException("Selecciona una isla valida (Tierra Bomba, Baru o Islas del Rosario).");
            }
            opcional(datos.descripcion(), "La descripcion no puede superar 300 caracteres.", 300);
        }

        if ("Agencia".equals(tipo)) {
            requerido(datos.razonSocial(), "La razon social es obligatoria.", 100);
            requerido(datos.nit(), "El NIT es obligatorio.", 20);
            requerido(datos.representante(), "El representante legal es obligatorio.", 100);
            requerido(datos.cedulaRepresentante(), "La cedula del representante es obligatoria.", 20);
            requerido(datos.rutaRnt(), "Adjunta el certificado RNT en PDF o imagen.", 255);
        }
    }

    public static String texto(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    private static void requerido(String valor, String mensaje, int maximo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        if (valor.length() > maximo) {
            throw new IllegalArgumentException(mensaje + " (maximo " + maximo + " caracteres).");
        }
    }

    private static void opcional(String valor, String mensaje, int maximo) {
        if (valor != null && valor.length() > maximo) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}
