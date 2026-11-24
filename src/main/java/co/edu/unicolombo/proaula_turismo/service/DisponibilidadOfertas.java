package co.edu.unicolombo.proaula_turismo.service;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import co.edu.unicolombo.proaula_turismo.model.Oferta;





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
        // Si no hay dias escritos, la oferta se ofrece todos los dias.
        if (dias == null || dias.isBlank()) {
            
            return true;
        }
        // getValue va de 1 (lunes) a 7 (domingo); por eso se resta 1.
        String diaBuscado = normalizar(DIAS.get(fecha.getDayOfWeek().getValue() - 1));
        // Los dias estan guardados como texto separado por comas.
        for (String valor : dias.split(",")) {
            if (normalizar(valor).equals(diaBuscado)) {
                return true;
            }
        }
        return false;
    }

    



    // LocalDate es una fecha sin hora, por ejemplo 2026-11-27.
    public static LocalDate primeraFechaCompatible(Collection<Oferta> ofertas,
            LocalDate desde, int diasBusqueda) {
        if (ofertas == null || ofertas.isEmpty() || desde == null || diasBusqueda < 1) {
            return null;
        }
        // Miramos hasta 90 dias hacia adelante, uno por uno.
        for (int i = 0; i < diasBusqueda; i++) {
            LocalDate fecha = desde.plusDays(i);
            // allMatch = TODAS las ofertas disponibles el mismo dia.
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

    // Quita acentos para comparar Miercoles con Miércoles.
    private static String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        String sinAcentos = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinAcentos.toLowerCase(Locale.ROOT);
    }
}
