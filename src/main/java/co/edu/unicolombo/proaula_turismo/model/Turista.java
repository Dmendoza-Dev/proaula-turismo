package co.edu.unicolombo.proaula_turismo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Turista")
public class Turista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_turista")
    private Integer idTurista;

    @Column(name = "Tipo_turista", nullable = false, length = 20)
    private String tipoTurista;

    @Column(name = "Nacionalidad", nullable = false, length = 60)
    private String nacionalidad;

    @Column(name = "Id_usuario", nullable = false, unique = true)
    private Integer idUsuario;

    public Turista() {
    }

    public Integer getIdTurista() {
        return idTurista;
    }

    public void setIdTurista(Integer idTurista) {
        this.idTurista = idTurista;
    }

    public String getTipoTurista() {
        return tipoTurista;
    }

    public void setTipoTurista(String tipoTurista) {
        this.tipoTurista = tipoTurista;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

}
