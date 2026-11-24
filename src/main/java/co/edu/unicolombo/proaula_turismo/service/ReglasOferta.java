package co.edu.unicolombo.proaula_turismo.service;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import co.edu.unicolombo.proaula_turismo.service.dto.OfertaForm;





public final class ReglasOferta {

    public static final Set<String> ISLAS = Set.of("Islas del Rosario", "Barú", "Tierra Bomba");

    public static final Set<String> CATEGORIAS = Set.of(
            "Alquiler de carpas y cabañas", "Almuerzo", "Hospedaje", "Actividades",
            "Pasadia", "Paquete", "Transporte");

    public static final List<String> DIAS = List.of(
            "Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado", "Domingo");

    private static final BigDecimal PRECIO_MAXIMO = new BigDecimal("9999999999.99");
    private static final int CUPOS_MAXIMOS = 100_000;

    private ReglasOferta() {
    }

    public static OfertaForm normalizarYValidar(OfertaForm datos) {
        if (datos == null) {
            throw new IllegalArgumentException("No se recibieron los datos de la oferta.");
        }

        String nombre = requerido(datos.nombre(), "El nombre es obligatorio.", 100);
        String categoria = requerido(datos.categoria(), "La categoria es obligatoria.", 50);
        // Solo se permiten categorias de la lista; nada de Hackeado.
        if (!CATEGORIAS.contains(categoria)) {
            throw new IllegalArgumentException("Selecciona una categoria valida.");
        }
        String isla = requerido(datos.isla(), "La isla es obligatoria.", 100);
        // Igual con la isla: solo Baru, Tierra Bomba o Islas del Rosario.
        if (!ISLAS.contains(isla)) {
            throw new IllegalArgumentException("Selecciona una isla valida (Islas del Rosario, Baru o Tierra Bomba).");
        }
        String descripcion = opcional(datos.descripcion(), "La descripcion no puede superar 500 caracteres.", 500);

        BigDecimal precio = datos.tarifa();
        // signum() dice si el precio es negativo, cero o positivo.
        if (precio == null || precio.signum() <= 0) {
            throw new IllegalArgumentException("La tarifa debe ser mayor a cero.");
        }
        // scale = numero de decimales; no se aceptan precios como 100.999.
        if (precio.scale() > 2) {
            throw new IllegalArgumentException("La tarifa admite maximo dos decimales.");
        }
        if (precio.compareTo(PRECIO_MAXIMO) > 0) {
            throw new IllegalArgumentException("La tarifa supera el maximo permitido.");
        }

        Integer cupos = datos.cupos();
        // Los cupos van de 1 a 100000: ni cero ni negativos.
        if (cupos == null || cupos < 1 || cupos > CUPOS_MAXIMOS) {
            throw new IllegalArgumentException("Los cupos deben estar entre 1 y " + CUPOS_MAXIMOS + ".");
        }

        List<String> dias = new ArrayList<>();
        if (datos.dias() != null) {
            // Recorremos los dias marcados en el formulario.
            for (String valor : datos.dias()) {
                String dia = texto(valor);
                if (dia == null) {
                    continue;
                }
                // Dias inventados como Festivo se rechazan.
                if (!DIAS.contains(dia)) {
                    throw new IllegalArgumentException("Dia no valido: " + dia + ".");
                }
                // Evita guardar el mismo dia dos veces.
                if (!dias.contains(dia)) {
                    dias.add(dia);
                }
            }
        }
        // Ordenamos Lunes, Martes... para que el texto sea siempre igual.
        dias.sort(Comparator.comparingInt(DIAS::indexOf));

        String inicioTexto = texto(datos.horarioInicio());
        String finTexto = texto(datos.horarioFin());
        // O se ponen las dos horas o ninguna; una sola esta mal.
        if ((inicioTexto == null) != (finTexto == null)) {
            throw new IllegalArgumentException("Indica la hora de inicio y la hora de fin.");
        }
        LocalTime inicio = hora(inicioTexto);
        LocalTime fin = hora(finTexto);
        // La hora de fin debe ser posterior, si no el horario no sirve.
        if (inicio != null && !fin.isAfter(inicio)) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio.");
        }

        return new OfertaForm(categoria, nombre, descripcion, isla, precio, cupos,
                dias, inicioTexto, finTexto, datos.imagenes());
    }

    // LocalTime es una hora sin fecha, por ejemplo 09:00.
    public static LocalTime hora(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(valor.trim());
        // abc no es una hora: el formato correcto es HH:mm.
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("La hora no tiene un formato valido (HH:mm).");
        }
    }

    private static String requerido(String valor, String mensaje, int maximo) {
        String limpio = texto(valor);
        if (limpio == null) {
            throw new IllegalArgumentException(mensaje);
        }
        if (limpio.length() > maximo) {
            throw new IllegalArgumentException(mensaje + " (maximo " + maximo + " caracteres).");
        }
        return limpio;
    }

    private static String opcional(String valor, String mensaje, int maximo) {
        String limpio = texto(valor);
        if (limpio != null && limpio.length() > maximo) {
            throw new IllegalArgumentException(mensaje);
        }
        return limpio;
    }

    private static String texto(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }
}
