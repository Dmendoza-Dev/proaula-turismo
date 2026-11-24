package co.edu.unicolombo.proaula_turismo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import co.edu.unicolombo.proaula_turismo.service.dto.RegistroUsuario;

class ReglasRegistroTest {

    @Test
    void aceptaUnTuristaConDatosValidos() {
        RegistroUsuario datos = turista("Turista@Ejemplo.com");
        RegistroUsuario limpio = ReglasRegistro.normalizar(datos, "Cliente");

        ReglasRegistro.validar(limpio, "Cliente");

        assertEquals("turista@ejemplo.com", limpio.correo());
        assertEquals("Turista", limpio.tipoTurista());
        assertEquals("Colombiana", limpio.nacionalidad());
    }

    @Test
    void rechazaUnCorreoConFormatoInvalido() {
        RegistroUsuario limpio = ReglasRegistro.normalizar(turista("correo-sin-arroba"), "Cliente");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> ReglasRegistro.validar(limpio, "Cliente"));

        assertEquals("El correo no tiene un formato valido.", error.getMessage());
    }

    @Test
    void rechazaUnaContrasenaCorta() {
        RegistroUsuario datos = turista("turista@ejemplo.com");
        RegistroUsuario corta = new RegistroUsuario(datos.nombres(), datos.documento(),
                datos.telefono(), datos.correo(), "corta", datos.tipoUsuario(),
                null, null, null, null, null, null, null, null, null, null, null);

        assertThrows(IllegalArgumentException.class,
                () -> ReglasRegistro.validar(corta, "Cliente"));
    }

    @Test
    void rechazaUnTipoDeServicioInventado() {
        RegistroUsuario proveedor = new RegistroUsuario("Proveedor", "12345678", null,
                "proveedor@ejemplo.com", "Clave123", "Proveedor", "Negocio", "Servicio inventado",
                "Barú", "Descripcion", null, null, null, null, null, null, null);

        assertThrows(IllegalArgumentException.class,
                () -> ReglasRegistro.validar(proveedor, "Proveedor"));
    }

    @Test
    void rechazaUnaIslaInventada() {
        RegistroUsuario proveedor = new RegistroUsuario("Proveedor", "12345678", null,
                "proveedor@ejemplo.com", "Clave123", "Proveedor", "Negocio", "Alimentación",
                "Marte", "Descripcion", null, null, null, null, null, null, null);

        assertThrows(IllegalArgumentException.class,
                () -> ReglasRegistro.validar(proveedor, "Proveedor"));
    }

    @Test
    void completaLaAgenciaConLosDatosDelRepresentante() {
        RegistroUsuario datos = new RegistroUsuario(null, null, "3001234567",
                "AGENCIA@QA.TEST", "Clave123", "Agencia", null, null, null, null,
                "Agencia QA", "NIT-1", "Representante Legal", "CC-1", "/uploads/rnt.pdf",
                null, null);

        RegistroUsuario limpio = ReglasRegistro.normalizar(datos, "Agencia");
        ReglasRegistro.validar(limpio, "Agencia");

        assertEquals("Representante Legal", limpio.nombres());
        assertEquals("CC-1", limpio.documento());
        assertEquals("agencia@qa.test", limpio.correo());
        assertEquals("Turista", limpio.tipoTurista());
        assertEquals("Colombiana", limpio.nacionalidad());
    }

    @Test
    void normalizaElTipoDeTurista() {
        assertEquals("Residente", ReglasRegistro.normalizarTipoTurista(" residente "));
        assertEquals("Turista", ReglasRegistro.normalizarTipoTurista("Extranjero"));
        assertEquals("Turista", ReglasRegistro.normalizarTipoTurista(null));
    }

    private RegistroUsuario turista(String correo) {
        return new RegistroUsuario("Ana Torres", "12345678", "3001234567", correo,
                "Clave123", "Cliente", null, null, null, null, null, null, null, null,
                null, "Turista", "Colombiana");
    }
}
