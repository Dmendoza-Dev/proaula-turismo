package co.edu.unicolombo.proaula_turismo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicolombo.proaula_turismo.service.ArchivoService;

@RestController
public class ArchivoController {

    private final ArchivoService archivos;

    public ArchivoController(ArchivoService archivos) {
        this.archivos = archivos;
    }

    @GetMapping("/uploads/{nombre}")
    public ResponseEntity<byte[]> descargar(@PathVariable String nombre) {
        try {
            // resolver revisa que el nombre no contenga ../ para salir de la carpeta.
            Path ruta = archivos.resolver(nombre);
            if (!Files.exists(ruta) || !Files.isRegularFile(ruta)) {
                return ResponseEntity.notFound().build();
            }
            // Adivina el tipo (PDF, PNG...) por la extension para que el navegador lo abra bien.
            MediaType tipo = MediaTypeFactory.getMediaType(nombre)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);
            return ResponseEntity.ok().contentType(tipo).body(Files.readAllBytes(ruta));
        // Si el archivo no se puede leer, se responde 404 en vez de un error feo.
        } catch (IOException | IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
