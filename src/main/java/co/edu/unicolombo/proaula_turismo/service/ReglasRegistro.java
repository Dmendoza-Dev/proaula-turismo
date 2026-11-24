package co.edu.unicolombo.proaula_turismo.service;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import co.edu.unicolombo.proaula_turismo.service.dto.RegistroUsuario;





public final class ReglasRegistro {

    public static final Set<String> ISLAS = Set.of("Tierra Bomba", "Barú", "Islas del Rosario");

    public static final Set<String> TIPOS_SERVICIO = Set.of(
            "Alimentación", "Carpas y playa", "Deportes acuáticos", "Transporte",
            "Hospedaje", "Artesanías", "Guía turístico", "Otro servicio");

    // Expresion regular: patron que exige algo@algo.punto.
    private static final Pattern CORREO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");

    private ReglasRegistro() {
    }

    public static String normalizarTipo(String tipo) {
        // Si no viene el tipo, se crea como cliente.
        if (tipo == null || tipo.isBlank()) {
            return "Cliente";
        }
        return switch (tipo.trim().toLowerCase(Locale.ROOT)) {
            case "proveedor" -> "Proveedor";
            case "agencia" -> "Agencia";
            case "cliente", "turista" -> "Cliente";
            // Nadie puede registrarse como supervisor desde la web.
            case "supervisor" -> throw new IllegalArgumentException("El registro de supervisores no esta habilitado.");
            default -> throw new IllegalArgumentException("Tipo de usuario no valido.");
        };
    }

    // Solo residente cuenta; cualquier otro valor queda como turista.
    public static String normalizarTipoTurista(String valor) {
        return valor != null && "residente".equalsIgnoreCase(valor.trim()) ? "Residente" : "Turista";
    }

    



    public static RegistroUsuario normalizar(RegistroUsuario datos, String tipo) {
        String nombres = texto(datos.nombres());
        String documento = texto(datos.documento());
        // Para agencias, nombre y documento se toman del representante si faltan.
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
                // El correo se guarda en minusculas para evitar duplicados por mayusculas.
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
                // Si no ponen nacionalidad, se asume Colombiana.
                nacionalidad == null ? "Colombiana" : nacionalidad);
    }

    public static void validar(RegistroUsuario datos, String tipo) {
        requerido(datos.nombres(), "El nombre es obligatorio.", 100);
        requerido(datos.documento(), "El documento es obligatorio.", 20);
        requerido(datos.correo(), "El correo es obligatorio.", 100);
        // matcher comprueba el correo con el patron de arriba.
        if (!CORREO.matcher(datos.correo()).matches()) {
            throw new IllegalArgumentException("El correo no tiene un formato valido.");
        }
        // La clave debe tener entre 8 y 72 caracteres (72 es el limite del cifrado).
        if (datos.contrasena() == null
                || datos.contrasena().length() < 8
                || datos.contrasena().length() > 72) {
            throw new IllegalArgumentException("La contrasena debe tener entre 8 y 72 caracteres.");
        }
        opcional(datos.telefono(), "El telefono no puede superar 20 caracteres.", 20);
        opcional(datos.nacionalidad(), "La nacionalidad no puede superar 60 caracteres.", 60);

        // Reglas extra solo para proveedores.
        if ("Proveedor".equals(tipo)) {
            requerido(datos.nombreNegocio(), "El nombre del negocio es obligatorio.", 100);
            requerido(datos.tipoServicio(), "El tipo de servicio es obligatorio.", 50);
            requerido(datos.isla(), "La isla es obligatoria.", 100);
            // La lista cerrada evita que se inventen tipos de servicio.
            if (!TIPOS_SERVICIO.contains(datos.tipoServicio())) {
                throw new IllegalArgumentException("Selecciona un tipo de servicio valido.");
            }
            // Marte no existe: solo se aceptan las tres islas permitidas.
            if (!ISLAS.contains(datos.isla())) {
                throw new IllegalArgumentException("Selecciona una isla valida (Tierra Bomba, Baru o Islas del Rosario).");
            }
            opcional(datos.descripcion(), "La descripcion no puede superar 300 caracteres.", 300);
        }

        // Las agencias deben traer NIT, representante y el PDF del RNT.
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

    // Solta un error claro si falta un dato obligatorio o se pasa de largo.
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
