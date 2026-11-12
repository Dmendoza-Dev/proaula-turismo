package co.edu.unicolombo.proaula_turismo.model;

public class Usuario {
    private Integer idUsuario;
    private String nombres;
    private String apellidos;
    private String documento;
    private String correo;
    private String telefono;
    private String contrasena;
    private String tipoUsuario;

    public Usuario() {
    }


    public Integer getIdUsuario() { 
        return idUsuario; 
    }
    public void setIdUsuario(Integer idUsuario) { 
        this.idUsuario = idUsuario; 
    }

    public String getNombres() { 
        return nombres; 
    }
    public void setNombres(String nombres) { 
        this.nombres = nombres; 
    }

    public String getApellidos() { 
        return apellidos; 
    }
    public void setApellidos(String apellidos) { 
        this.apellidos = apellidos;
    }

    public String getDocumento() { 
        return documento; 
    }
    public void setDocumento(String documento) { 
        this.documento = documento; 
    }

    public String getCorreo() { 
        return correo; 
    }
    public void setCorreo(String correo) { 
        this.correo = correo; 
    }

    public String getTelefono() { 
        return telefono; 
    }
    public void setTelefono(String telefono) { 
        this.telefono = telefono; 
    }

    public String getContrasena() { 
        return contrasena; 
    }
    public void setContrasena(String contrasena) { 
        this.contrasena = contrasena; 
    }

    public String getTipoUsuario() { 
        return tipoUsuario; 
    }
    public void setTipoUsuario(String tipoUsuario) { 
        this.tipoUsuario = tipoUsuario; 
    }
}