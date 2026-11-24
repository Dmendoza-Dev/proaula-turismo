package co.edu.unicolombo.proaula_turismo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import co.edu.unicolombo.proaula_turismo.service.dto.OfertaForm;

class ReglasOfertaTest {

    @Test
    void aceptaYNormalizaUnaOfertaValida() {
        OfertaForm datos = oferta("Barú", "Almuerzo", List.of("Viernes", "Lunes"),
                "08:00", "17:00", new BigDecimal("150000.00"), "Almuerzo tipico");

        OfertaForm limpio = ReglasOferta.normalizarYValidar(datos);

        assertEquals("Barú", limpio.isla());
        assertEquals(List.of("Lunes", "Viernes"), limpio.dias());
        assertEquals(new BigDecimal("150000.00"), limpio.tarifa());
    }

    @Test
    void aceptaUnaOfertaSinDiasNiHorario() {
        OfertaForm limpio = ReglasOferta.normalizarYValidar(
                oferta("Tierra Bomba", "Actividades", List.of(), null, null,
                        new BigDecimal("10000"), null));

        assertEquals(List.of(), limpio.dias());
        assertNull(limpio.horarioInicio());
        assertNull(limpio.horarioFin());
    }

    @Test
    void rechazaUnaIslaInventada() {
        OfertaForm datos = oferta("Marte", "Almuerzo", List.of(),
                null, null, new BigDecimal("10000"), null);

        assertThrows(IllegalArgumentException.class,
                () -> ReglasOferta.normalizarYValidar(datos));
    }

    @Test
    void rechazaUnaCategoriaInventada() {
        OfertaForm datos = oferta("Barú", "Hackeo", List.of(),
                null, null, new BigDecimal("10000"), null);

        assertThrows(IllegalArgumentException.class,
                () -> ReglasOferta.normalizarYValidar(datos));
    }

    @Test
    void rechazaUnDiaInventado() {
        OfertaForm datos = oferta("Barú", "Almuerzo", List.of("Festivo"),
                null, null, new BigDecimal("10000"), null);

        assertThrows(IllegalArgumentException.class,
                () -> ReglasOferta.normalizarYValidar(datos));
    }

    @Test
    void rechazaUnaHoraConFormatoInvalido() {
        OfertaForm datos = oferta("Barú", "Almuerzo", List.of(),
                "abc", "17:00", new BigDecimal("10000"), null);

        assertThrows(IllegalArgumentException.class,
                () -> ReglasOferta.normalizarYValidar(datos));
    }

    @Test
    void rechazaUnaHoraDeFinAnteriorAlInicio() {
        OfertaForm datos = oferta("Barú", "Almuerzo", List.of(),
                "18:00", "07:00", new BigDecimal("10000"), null);

        assertThrows(IllegalArgumentException.class,
                () -> ReglasOferta.normalizarYValidar(datos));
    }

    @Test
    void rechazaUnaHoraSinSuPareja() {
        OfertaForm datos = oferta("Barú", "Almuerzo", List.of(),
                "08:00", null, new BigDecimal("10000"), null);

        assertThrows(IllegalArgumentException.class,
                () -> ReglasOferta.normalizarYValidar(datos));
    }

    @Test
    void rechazaUnaTarifaConTresDecimales() {
        OfertaForm datos = oferta("Barú", "Almuerzo", List.of(),
                null, null, new BigDecimal("100.999"), null);

        assertThrows(IllegalArgumentException.class,
                () -> ReglasOferta.normalizarYValidar(datos));
    }

    @Test
    void rechazaUnaDescripcionDemasiadoLarga() {
        OfertaForm datos = oferta("Barú", "Almuerzo", List.of(),
                null, null, new BigDecimal("10000"), "x".repeat(501));

        assertThrows(IllegalArgumentException.class,
                () -> ReglasOferta.normalizarYValidar(datos));
    }

    private OfertaForm oferta(String isla, String categoria, List<String> dias,
            String inicio, String fin, BigDecimal precio, String descripcion) {
        return new OfertaForm(categoria, "Tour de prueba", descripcion, isla,
                precio, 5, dias, inicio, fin, List.of());
    }
}
