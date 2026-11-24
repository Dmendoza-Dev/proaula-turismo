package co.edu.unicolombo.proaula_turismo.service;

import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicolombo.proaula_turismo.model.Agencia;
import co.edu.unicolombo.proaula_turismo.model.Proveedor;
import co.edu.unicolombo.proaula_turismo.model.Turista;
import co.edu.unicolombo.proaula_turismo.model.Usuario;
import co.edu.unicolombo.proaula_turismo.repository.AgenciaRepository;
import co.edu.unicolombo.proaula_turismo.repository.ProveedorRepository;
import co.edu.unicolombo.proaula_turismo.repository.TuristaRepository;
import co.edu.unicolombo.proaula_turismo.repository.UsuarioRepository;
import co.edu.unicolombo.proaula_turismo.service.dto.RegistroUsuario;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final TuristaRepository turistas;
    private final ProveedorRepository proveedores;
    private final AgenciaRepository agencias;
    private final PasswordHasher hasher;

    public UsuarioService(UsuarioRepository usuarios, TuristaRepository turistas,
            ProveedorRepository proveedores, AgenciaRepository agencias, PasswordHasher hasher) {
        this.usuarios = usuarios;
        this.turistas = turistas;
        this.proveedores = proveedores;
        this.agencias = agencias;
        this.hasher = hasher;
    }

    @Transactional
    public void registrar(RegistroUsuario datos) {
        String tipo = normalizarTipo(datos.tipoUsuario());
        validar(datos, tipo);

        Usuario usuario = new Usuario();
        usuario.setNombres(datos.nombres().trim());
        usuario.setDocumento(datos.documento().trim());
        usuario.setCorreo(datos.correo().trim().toLowerCase(Locale.ROOT));
        usuario.setTelefono(datos.telefono());
        usuario.setContrasena(hasher.generar(datos.contrasena()));
        usuario.setTipoUsuario(tipo);
        usuarios.save(usuario);

        switch (tipo) {
            case "Cliente" -> {
                Turista turista = new Turista();
                turista.setTipoTurista("Turista");
                turista.setNacionalidad("Colombiana");
                turista.setIdUsuario(usuario.getIdUsuario());
                turistas.save(turista);
            }
            case "Proveedor" -> {
                Proveedor proveedor = new Proveedor();
                proveedor.setNombreNegocio(datos.nombreNegocio());
                proveedor.setTipoServicio(datos.tipoServicio());
                proveedor.setIsla(datos.isla());
                proveedor.setDescripcion(datos.descripcion());
                proveedor.setIdUsuario(usuario.getIdUsuario());
                proveedores.save(proveedor);
            }
            case "Agencia" -> {
                Agencia agencia = new Agencia();
                agencia.setRazonSocial(datos.razonSocial());
                agencia.setNit(datos.nit());
                agencia.setRepresentante(datos.representante());
                agencia.setCedulaRepresentante(datos.cedulaRepresentante());
                agencia.setRnt(datos.rutaRnt());
                agencia.setIdUsuario(usuario.getIdUsuario());
                agencias.save(agencia);
            }
            default -> throw new IllegalArgumentException("Tipo de usuario no valido.");
        }
    }

    public Optional<Usuario> autenticar(String correo, String contrasena) {
        if (correo == null || contrasena == null) {
            return Optional.empty();
        }
        Optional<Usuario> encontrado = usuarios.findByCorreo(correo.trim().toLowerCase(Locale.ROOT));
        if (encontrado.isEmpty() || !hasher.verificar(contrasena, encontrado.get().getContrasena())) {
            return Optional.empty();
        }
        return encontrado;
    }

    private void validar(RegistroUsuario datos, String tipo) {
        if (datos.nombres() == null || datos.nombres().isBlank()
                || datos.documento() == null || datos.documento().isBlank()
                || datos.correo() == null || datos.correo().isBlank()
                || datos.contrasena() == null || datos.contrasena().length() < 8) {
            throw new IllegalArgumentException("Revisa los datos personales y la contrasena (minimo 8 caracteres).");
        }
        if (usuarios.existsByCorreo(datos.correo().trim().toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese correo.");
        }
        if (usuarios.existsByDocumento(datos.documento().trim())) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese documento.");
        }
        if (tipo.equals("Proveedor") && (datos.nombreNegocio() == null || datos.tipoServicio() == null
                || datos.isla() == null)) {
            throw new IllegalArgumentException("Faltan datos del negocio.");
        }
        if (tipo.equals("Agencia") && (datos.razonSocial() == null || datos.nit() == null
                || datos.representante() == null || datos.cedulaRepresentante() == null
                || datos.rutaRnt() == null)) {
            throw new IllegalArgumentException("Faltan datos de la agencia o del certificado RNT.");
        }
    }

    private String normalizarTipo(String tipo) {
        if (tipo == null) {
            return "Cliente";
        }
        String valor = tipo.trim().toLowerCase(Locale.ROOT);
        return switch (valor) {
            case "proveedor" -> "Proveedor";
            case "agencia" -> "Agencia";
            default -> "Cliente";
        };
    }
}
