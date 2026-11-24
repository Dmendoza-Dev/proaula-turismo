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

    public UsuarioService(UsuarioRepository usuarios, TuristaRepository turistas,
            ProveedorRepository proveedores, AgenciaRepository agencias) {
        this.usuarios = usuarios;
        this.turistas = turistas;
        this.proveedores = proveedores;
        this.agencias = agencias;
    }

    @Transactional
    public void registrar(RegistroUsuario datos) {
        String tipo = ReglasRegistro.normalizarTipo(datos.tipoUsuario());
        RegistroUsuario limpio = ReglasRegistro.normalizar(datos, tipo);
        ReglasRegistro.validar(limpio, tipo);

        if (usuarios.existsByCorreo(limpio.correo())) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese correo.");
        }
        if (usuarios.existsByDocumento(limpio.documento())) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese documento.");
        }
        if ("Agencia".equals(tipo) && agencias.existsByNit(limpio.nit())) {
            throw new IllegalArgumentException("Ya existe una agencia con ese NIT.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombres(limpio.nombres());
        usuario.setDocumento(limpio.documento());
        usuario.setCorreo(limpio.correo());
        usuario.setTelefono(limpio.telefono());
        // Prototipo academico: se guarda tal cual para poder leerla en la BD.
        usuario.setContrasena(limpio.contrasena());
        usuario.setTipoUsuario(tipo);
        usuarios.saveAndFlush(usuario);

        switch (tipo) {
            case "Cliente" -> {
                Turista turista = new Turista();
                turista.setTipoTurista(limpio.tipoTurista());
                turista.setNacionalidad(limpio.nacionalidad());
                turista.setIdUsuario(usuario.getIdUsuario());
                turistas.save(turista);
            }
            case "Proveedor" -> {
                Proveedor proveedor = new Proveedor();
                proveedor.setNombreNegocio(limpio.nombreNegocio());
                proveedor.setTipoServicio(limpio.tipoServicio());
                proveedor.setIsla(limpio.isla());
                proveedor.setDescripcion(limpio.descripcion());
                proveedor.setIdUsuario(usuario.getIdUsuario());
                proveedores.save(proveedor);
            }
            case "Agencia" -> {
                Agencia agencia = new Agencia();
                agencia.setRazonSocial(limpio.razonSocial());
                agencia.setNit(limpio.nit());
                agencia.setRepresentante(limpio.representante());
                agencia.setCedulaRepresentante(limpio.cedulaRepresentante());
                agencia.setRnt(limpio.rutaRnt());
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
        if (encontrado.isEmpty() || !encontrado.get().getContrasena().equals(contrasena)) {
            return Optional.empty();
        }
        return encontrado;
    }
}
