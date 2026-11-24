package co.edu.unicolombo.proaula_turismo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MetodosPagoTest {

    @Test
    void aceptaUnMetodoPermitidoYDevuelveSuNombreCanonico() {
        assertEquals("PSE", MetodosPago.normalizar(" pse "));
        assertEquals("Tarjeta de crédito", MetodosPago.normalizar("TARJETA DE CRÉDITO"));
        assertEquals("Efectivo", MetodosPago.normalizar("Efectivo"));
    }

    @Test
    void rechazaUnMetodoInventado() {
        assertThrows(IllegalArgumentException.class, () -> MetodosPago.normalizar("Hackeado"));
    }

    @Test
    void rechazaUnMetodoVacio() {
        assertThrows(IllegalArgumentException.class, () -> MetodosPago.normalizar(null));
        assertThrows(IllegalArgumentException.class, () -> MetodosPago.normalizar("   "));
    }
}
