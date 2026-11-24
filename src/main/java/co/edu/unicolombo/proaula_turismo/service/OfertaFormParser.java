package co.edu.unicolombo.proaula_turismo.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import co.edu.unicolombo.proaula_turismo.service.dto.OfertaForm;

/** Convierte los valores de texto del formulario en tipos de Java. */
@Component
public class OfertaFormParser {

    public OfertaForm parsear(String categoria, String nombre, String descripcion, String isla,
            String tarifa, String cupos, List<String> dias, String horarioInicio,
            String horarioFin, List<MultipartFile> imagenes) {
        BigDecimal precio;
        int capacidad;
        try {
            precio = new BigDecimal(tarifa == null ? "" : tarifa.trim());
            capacidad = Integer.parseInt(cupos == null ? "" : cupos.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Tarifa y cupos deben ser numeros validos.");
        }
        return new OfertaForm(categoria, nombre, descripcion, isla, precio, capacidad,
                dias, horarioInicio, horarioFin, imagenes);
    }
}
