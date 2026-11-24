package co.edu.unicolombo.proaula_turismo.service;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import co.edu.unicolombo.proaula_turismo.model.Oferta;

/**
 * Comprueba en que dias se puede disfrutar una oferta y busca la primera fecha
 * en la que todas las ofertas del carrito estan disponibles.
 */
public final class DisponibilidadOfertas {

    private static final List<String> DIAS = List.of(
            "Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado", "Domingo");

    private DisponibilidadOfertas() {
    }

    public static boolean disponibleEn(Oferta oferta, LocalDate fecha) {
        if (oferta == null || fecha == null) {
            return false;
        }
        String dias = oferta.getDiasDisponibles();
        if (dias == null || dias.isBlank()) {
            // Sin restriccion explicita: disponible todos los dias.
            return true;
        }
        String diaBuscado = normalizar(DIAS.get(fecha.getDayOfWeek().getValue() - 1));
        for (String valor : dias.split(",")) {
            if (normalizar(valor).equals(diaBuscado)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Devuelve la primera fecha, desde {@code desde}, en la que todas las
     * ofertas estan disponibles; o null si no hay coincidencia en el rango.
     */
    public static LocalDate primeraFechaCompatible(Collection<Oferta> ofertas,
            LocalDate desde, int diasBusqueda) {
        if (ofertas == null || ofertas.isEmpty() || desde == null || diasBusqueda < 1) {
            return null;
        }
        for (int i = 0; i < diasBusqueda; i++) {
            LocalDate fecha = desde.plusDays(i);
            if (ofertas.stream().allMatch(oferta -> disponibleEn(oferta, fecha))) {
                return fecha;
            }
        }
        return null;
    }

    public static String diaDe(LocalDate fecha) {
        DayOfWeek dia = fecha.getDayOfWeek();
        return DIAS.get(dia.getValue() - 1);
    }

    private static String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        String sinAcentos = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinAcentos.toLowerCase(Locale.ROOT);
    }
}
