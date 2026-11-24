package co.edu.unicolombo.proaula_turismo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import co.edu.unicolombo.proaula_turismo.model.Oferta;

class DisponibilidadOfertasTest {

    private static final LocalDate LUNES = LocalDate.of(2026, 11, 23);

    @Test
    void reconoceElDiaDisponible() {
        Oferta oferta = oferta("Lunes");

        assertTrue(DisponibilidadOfertas.disponibleEn(oferta, LUNES));
        assertFalse(DisponibilidadOfertas.disponibleEn(oferta, LUNES.plusDays(1)));
    }

    @Test
    void ignoraAcentosYMayusculas() {
        Oferta oferta = oferta("MIÉRCOLES");

        assertTrue(DisponibilidadOfertas.disponibleEn(oferta, LUNES.plusDays(2)));
    }

    @Test
    void unaOfertaSinDiasEstaDisponibleSiempre() {
        assertTrue(DisponibilidadOfertas.disponibleEn(oferta(null), LUNES));
        assertTrue(DisponibilidadOfertas.disponibleEn(oferta(" "), LUNES));
    }

    @Test
    void encuentraLaMismaFechaSiCumpleLosDias() {
        LocalDate fecha = DisponibilidadOfertas.primeraFechaCompatible(
                List.of(oferta("Lunes")), LUNES, 7);

        assertEquals(LUNES, fecha);
    }

    @Test
    void buscaLaSiguienteFechaCompatible() {
        LocalDate fecha = DisponibilidadOfertas.primeraFechaCompatible(
                List.of(oferta("Lunes")), LUNES.plusDays(1), 7);

        assertEquals(LUNES.plusDays(7), fecha);
    }

    @Test
    void encuentraElDiaComunEntreVariasOfertas() {
        LocalDate fecha = DisponibilidadOfertas.primeraFechaCompatible(
                List.of(oferta("Lunes, Miercoles"), oferta("Miercoles, Viernes")),
                LUNES, 7);

        assertEquals(LUNES.plusDays(2), fecha);
    }

    @Test
    void devuelveNullSiNoHayDiaComun() {
        LocalDate fecha = DisponibilidadOfertas.primeraFechaCompatible(
                List.of(oferta("Lunes"), oferta("Martes")), LUNES, 7);

        assertNull(fecha);
    }

    @Test
    void devuelveNullConUnCarritoVacio() {
        assertNull(DisponibilidadOfertas.primeraFechaCompatible(List.of(), LUNES, 7));
    }

    private Oferta oferta(String dias) {
        Oferta oferta = new Oferta();
        oferta.setDiasDisponibles(dias);
        return oferta;
    }
}
