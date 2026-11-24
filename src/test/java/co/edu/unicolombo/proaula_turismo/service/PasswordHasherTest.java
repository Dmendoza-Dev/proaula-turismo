package co.edu.unicolombo.proaula_turismo.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    private final PasswordHasher hasher = new PasswordHasher();

    @Test
    void guardaUnHashYAceptaLaContrasenaCorrecta() {
        String guardada = hasher.generar("Contrasena123");
        assertNotEquals("Contrasena123", guardada);
        assertTrue(hasher.verificar("Contrasena123", guardada));
    }

    @Test
    void rechazaUnaContrasenaDistinta() {
        String guardada = hasher.generar("Contrasena123");
        assertFalse(hasher.verificar("OtraContrasena123", guardada));
    }
}
