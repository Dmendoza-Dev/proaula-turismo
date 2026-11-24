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

    // Transaccion = si algo falla en medio, se deshace todo lo guardado.
    @Transactional
    public void registrar(RegistroUsuario datos) {
        // Primero convertimos el texto del formulario en Cliente, Proveedor o Agencia.
        String tipo = ReglasRegistro.normalizarTipo(datos.tipoUsuario());
        RegistroUsuario limpio = ReglasRegistro.normalizar(datos, tipo);
        ReglasRegistro.validar(limpio, tipo);

        // Consulta a la BD: ya hay alguien con este correo?
        if (usuarios.existsByCorreo(limpio.correo())) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese correo.");
        }
        // Consulta a la BD: ya hay alguien con este documento?
        if (usuarios.existsByDocumento(limpio.documento())) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese documento.");
        }
        // El NIT de agencia es unico; si se repite se avisa antes de grabar.
        if ("Agencia".equals(tipo) && agencias.existsByNit(limpio.nit())) {
            throw new IllegalArgumentException("Ya existe una agencia con ese NIT.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombres(limpio.nombres());
        usuario.setDocumento(limpio.documento());
        usuario.setCorreo(limpio.correo());
        usuario.setTelefono(limpio.telefono());
        
        usuario.setContrasena(limpio.contrasena());
        usuario.setTipoUsuario(tipo);
        // saveAndFlush guarda y confirma ya para obtener el id generado.
        usuarios.saveAndFlush(usuario);

        // En la misma transaccion se crea la fila segun el tipo de cuenta.
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

    // Optional = puede venir un usuario o venir vacio, sin devolver null.
    public Optional<Usuario> autenticar(String correo, String contrasena) {
        if (correo == null || contrasena == null) {
            return Optional.empty();
        }
        // Busca en la BD por correo en minusculas.
        Optional<Usuario> encontrado = usuarios.findByCorreo(correo.trim().toLowerCase(Locale.ROOT));
        // Comparamos la clave tal cual; si no coincide, no hay login.
        if (encontrado.isEmpty() || !encontrado.get().getContrasena().equals(contrasena)) {
            return Optional.empty();
        }
        return encontrado;
    }
}
