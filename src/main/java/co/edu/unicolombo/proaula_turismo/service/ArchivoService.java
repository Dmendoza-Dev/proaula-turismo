package co.edu.unicolombo.proaula_turismo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** Guarda los archivos subidos en una carpeta externa al JAR. */
@Service
public class ArchivoService {

    private static final long TAMANO_MAXIMO = 5 * 1024 * 1024;
    private static final Set<String> PERMITIDAS = Set.of("pdf", "jpg", "jpeg", "png", "webp");

    @Value("${app.upload.dir:uploads}")
    private String carpetaConfigurada;

    public String guardar(MultipartFile archivo, String prefijo) {
        if (archivo == null || archivo.isEmpty()) {
            return null;
        }
        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new IllegalArgumentException("El archivo supera el limite de 5 MB.");
        }

        String original = archivo.getOriginalFilename();
        String extension = original != null && original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase()
                : "";
        if (!PERMITIDAS.contains(extension)) {
            throw new IllegalArgumentException("Formato de archivo no permitido.");
        }

        String nombre = prefijo + "-" + UUID.randomUUID() + "." + extension;
        try {
            Path carpeta = carpeta();
            Files.createDirectories(carpeta);
            Files.copy(archivo.getInputStream(), carpeta.resolve(nombre),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar el archivo.", e);
        }
        return "/uploads/" + nombre;
    }

    public Path resolver(String nombre) {
        Path carpeta = carpeta();
        Path ruta = carpeta.resolve(nombre).normalize();
        if (!ruta.startsWith(carpeta)) {
            throw new IllegalArgumentException("Ruta de archivo no valida.");
        }
        return ruta;
    }

    public void borrar(String url) {
        if (url == null || !url.startsWith("/uploads/")) {
            return;
        }
        try {
            Files.deleteIfExists(resolver(url.substring("/uploads/".length())));
        } catch (IOException ignored) {
            // Si el archivo ya no existe no afecta la operacion.
        }
    }

    private Path carpeta() {
        return Paths.get(carpetaConfigurada).toAbsolutePath().normalize();
    }
}
