package co.edu.unicolombo.proaula_turismo.service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.stereotype.Component;

/**
 * Genera y verifica contrasenas con PBKDF2 (estandar de Java).
 * La BD nunca guarda la contrasena escrita por el usuario.
 */
@Component
public class PasswordHasher {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 120_000;
    private static final int LONGITUD_CLAVE = 256;
    private static final int LONGITUD_SAL = 16;

    public String generar(String contrasena) {
        byte[] sal = new byte[LONGITUD_SAL];
        new SecureRandom().nextBytes(sal);
        byte[] clave = derivar(contrasena, sal);
        return ITERACIONES + ":" + Base64.getEncoder().encodeToString(sal)
                + ":" + Base64.getEncoder().encodeToString(clave);
    }

    public boolean verificar(String contrasena, String guardada) {
        if (contrasena == null || guardada == null) {
            return false;
        }
        try {
            String[] partes = guardada.split(":", 3);
            if (partes.length != 3) {
                return false;
            }
            int iteraciones = Integer.parseInt(partes[0]);
            byte[] sal = Base64.getDecoder().decode(partes[1]);
            byte[] esperada = Base64.getDecoder().decode(partes[2]);
            byte[] actual = derivar(contrasena, sal, iteraciones);
            return MessageDigest.isEqual(esperada, actual);
        } catch (Exception e) {
            return false;
        }
    }

    private byte[] derivar(String contrasena, byte[] sal) {
        return derivar(contrasena, sal, ITERACIONES);
    }

    private byte[] derivar(String contrasena, byte[] sal, int iteraciones) {
        try {
            PBEKeySpec especificacion = new PBEKeySpec(
                    contrasena.toCharArray(), sal, iteraciones, LONGITUD_CLAVE);
            try {
                return SecretKeyFactory.getInstance(ALGORITMO)
                        .generateSecret(especificacion).getEncoded();
            } finally {
                especificacion.clearPassword();
            }
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo procesar la contrasena", e);
        }
    }
}
