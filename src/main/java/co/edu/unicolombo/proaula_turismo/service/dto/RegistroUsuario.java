package co.edu.unicolombo.proaula_turismo.service.dto;

import org.springframework.web.multipart.MultipartFile;


// Todos los datos que vienen del formulario de registro, antes de limpiarlos.
public record RegistroUsuario(
        String nombres,
        String documento,
        String telefono,
        String correo,
        String contrasena,
        String tipoUsuario,
        String nombreNegocio,
        String tipoServicio,
        String isla,
        String descripcion,
        String razonSocial,
        String nit,
        String representante,
        String cedulaRepresentante,
        String rutaRnt,
        String tipoTurista,
        String nacionalidad) {
}
