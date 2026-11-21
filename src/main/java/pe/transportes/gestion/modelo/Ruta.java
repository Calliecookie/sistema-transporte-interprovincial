package pe.transportes.gestion.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "ruta")
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "origen_id", nullable = false)
    private Destino origen;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "destino_id", nullable = false)
    private Destino destino;

    @NotNull(message = "La distancia es obligatoria")
    @Min(value = 1, message = "La distancia debe ser de al menos 1 km")
    @Max(value = 3000, message = "La distancia no puede superar los 3000 km")
    @Column(name = "distancia_km", nullable = false)
    private Integer distanciaKm;

    @NotNull(message = "La duración es obligatoria")
    @DecimalMin(value = "0.5", message = "La duración mínima es de 0.5 horas")
    @DecimalMax(value = "48.0", message = "La duración máxima es de 48 horas")
    @Digits(integer = 2, fraction = 1, message = "La duración admite un solo decimal")
    @Column(name = "duracion_horas", nullable = false, precision = 4, scale = 1)
    private BigDecimal duracionHoras;

    @NotNull(message = "El precio base es obligatorio")
    @DecimalMin(value = "1.00", message = "El precio base debe ser mayor a cero")
    @DecimalMax(value = "9999.99", message = "El precio base es demasiado alto")
    @Digits(integer = 4, fraction = 2, message = "El precio admite hasta 2 decimales")
    @Column(name = "precio_base", nullable = false, precision = 8, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "activa", nullable = false)
    private boolean activa = true;

    @NotNull(message = "Seleccione el origen")
    public Long getIdOrigen() {
        return origen == null ? null : origen.getId();
    }

    @NotNull(message = "Seleccione el destino")
    public Long getIdDestino() {
        return destino == null ? null : destino.getId();
    }

    public String getDescripcion() {
        String nombreOrigen = origen == null ? "" : origen.getNombre();
        String nombreDestino = destino == null ? "" : destino.getNombre();
        return nombreOrigen + " → " + nombreDestino;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Destino getOrigen() {
        return origen;
    }

    public void setOrigen(Destino origen) {
        this.origen = origen;
    }

    public Destino getDestino() {
        return destino;
    }

    public void setDestino(Destino destino) {
        this.destino = destino;
    }

    public Integer getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(Integer distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    public BigDecimal getDuracionHoras() {
        return duracionHoras;
    }

    public void setDuracionHoras(BigDecimal duracionHoras) {
        this.duracionHoras = duracionHoras;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(BigDecimal precioBase) {
        this.precioBase = precioBase;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}
