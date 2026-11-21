package pe.transportes.gestion.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "viaje")
public class Viaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "conductor_id", nullable = false)
    private Conductor conductor;

    @NotNull(message = "La fecha y hora de salida son obligatorias")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "fecha_salida", nullable = false)
    private LocalDateTime fechaSalida;

    @Column(name = "fecha_llegada", nullable = false)
    private LocalDateTime fechaLlegada;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "1.00", message = "El precio debe ser mayor a cero")
    @DecimalMax(value = "9999.99", message = "El precio es demasiado alto")
    @Digits(integer = 4, fraction = 2, message = "El precio admite hasta 2 decimales")
    @Column(name = "precio", nullable = false, precision = 8, scale = 2)
    private BigDecimal precio;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoViaje estado = EstadoViaje.PROGRAMADO;

    @Transient
    private int asientosLibres;

    @NotNull(message = "Seleccione una ruta")
    public Long getIdRuta() {
        return ruta == null ? null : ruta.getId();
    }

    @NotNull(message = "Seleccione un bus")
    public Long getIdBus() {
        return bus == null ? null : bus.getId();
    }

    @NotNull(message = "Seleccione un conductor")
    public Long getIdConductor() {
        return conductor == null ? null : conductor.getId();
    }

    public int getAsientosTotales() {
        return bus == null || bus.getCapacidad() == null ? 0 : bus.getCapacidad();
    }

    public int getAsientosOcupados() {
        return getAsientosTotales() - asientosLibres;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ruta getRuta() {
        return ruta;
    }

    public void setRuta(Ruta ruta) {
        this.ruta = ruta;
    }

    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public Conductor getConductor() {
        return conductor;
    }

    public void setConductor(Conductor conductor) {
        this.conductor = conductor;
    }

    public LocalDateTime getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDateTime fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public LocalDateTime getFechaLlegada() {
        return fechaLlegada;
    }

    public void setFechaLlegada(LocalDateTime fechaLlegada) {
        this.fechaLlegada = fechaLlegada;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public EstadoViaje getEstado() {
        return estado;
    }

    public void setEstado(EstadoViaje estado) {
        this.estado = estado;
    }

    public int getAsientosLibres() {
        return asientosLibres;
    }

    public void setAsientosLibres(int asientosLibres) {
        this.asientosLibres = asientosLibres;
    }
}
