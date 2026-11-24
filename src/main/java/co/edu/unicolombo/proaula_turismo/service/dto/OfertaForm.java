package co.edu.unicolombo.proaula_turismo.service.dto;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

/** Datos del formulario de publicacion y edicion de ofertas. */
public record OfertaForm(
        String categoria,
        String nombre,
        String descripcion,
        String isla,
        BigDecimal tarifa,
        Integer cupos,
        List<String> dias,
        String horarioInicio,
        String horarioFin,
        List<MultipartFile> imagenes) {
}
