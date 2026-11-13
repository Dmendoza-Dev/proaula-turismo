package co.edu.unicolombo.proaula_turismo.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Oferta")
public class Oferta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_oferta")
    private Integer idOferta;

    @Column(name = "Nombre_oferta", nullable = false, length = 100)
    private String nombreOferta;

    @Column(name = "Descripcion", length = 500)
    private String descripcion;

    @Column(name = "Categoria", nullable = false, length = 50)
    private String categoria;

    @Column(name = "Isla", nullable = false, length = 100)
    private String isla;

    @Column(name = "Precio", nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(name = "Cupos_maximos", nullable = false)
    private Integer cuposMaximos;

    @Column(name = "Estado_oferta", length = 20)
    private String estadoOferta;

    @Column(name = "Id_proveedor", nullable = false)
    private Integer idProveedor;

    public Oferta() {
    }

    public Integer getIdOferta() {
        return idOferta;
    }

    public void setIdOferta(Integer idOferta) {
        this.idOferta = idOferta;
    }

    public String getNombreOferta() {
        return nombreOferta;
    }

    public void setNombreOferta(String nombreOferta) {
        this.nombreOferta = nombreOferta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getIsla() {
        return isla;
    }

    public void setIsla(String isla) {
        this.isla = isla;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getCuposMaximos() {
        return cuposMaximos;
    }

    public void setCuposMaximos(Integer cuposMaximos) {
        this.cuposMaximos = cuposMaximos;
    }

    public String getEstadoOferta() {
        return estadoOferta;
    }

    public void setEstadoOferta(String estadoOferta) {
        this.estadoOferta = estadoOferta;
    }

    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }
}