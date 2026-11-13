package co.edu.unicolombo.proaula_turismo.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Itinerario")
public class Itinerario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_itinerario")
    private Integer idItinerario;

    @Column(name = "Nombre_itinerario", nullable = false, length = 100)
    private String nombreItinerario;

    @Column(name = "Destino", nullable = false, length = 150)
    private String destino;

    @Column(name = "Fecha_visita", nullable = false)
    private LocalDate fechaVisita;

    @Column(name = "Num_personas", nullable = false)
    private int numPersonas;

    @Column(name = "Costo_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoTotal;

    @Column(name = "Estado_itinerario", length = 30)
    private String estadoItinerario;

    @Column(name = "Id_cliente", nullable = false)
    private Integer idCliente;

    public Itinerario() {
    }

    public Integer getIdItinerario() {
        return idItinerario;
    }

    public void setIdItinerario(Integer idItinerario) {
        this.idItinerario = idItinerario;
    }

    public String getNombreItinerario() {
        return nombreItinerario;
    }

    public void setNombreItinerario(String nombreItinerario) {
        this.nombreItinerario = nombreItinerario;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public LocalDate getFechaVisita() {
        return fechaVisita;
    }

    public void setFechaVisita(LocalDate fechaVisita) {
        this.fechaVisita = fechaVisita;
    }

    public int getNumPersonas() {
        return numPersonas;
    }

    public void setNumPersonas(int numPersonas) {
        this.numPersonas = numPersonas;
    }

    public BigDecimal getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(BigDecimal costoTotal) {
        this.costoTotal = costoTotal;
    }

    public String getEstadoItinerario() {
        return estadoItinerario;
    }

    public void setEstadoItinerario(String estadoItinerario) {
        this.estadoItinerario = estadoItinerario;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

}