package pe.transportes.gestion.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "bus")
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La placa es obligatoria")
    @Pattern(regexp = "[A-Za-z0-9]{3}-[A-Za-z0-9]{3}", message = "La placa debe tener el formato ABC-123")
    @Column(name = "placa", nullable = false, unique = true, length = 7)
    private String placa;

    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 40, message = "La marca no puede superar los 40 caracteres")
    @Column(name = "marca", nullable = false, length = 40)
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 40, message = "El modelo no puede superar los 40 caracteres")
    @Column(name = "modelo", nullable = false, length = 40)
    private String modelo;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 20, message = "La capacidad mínima es de 20 asientos")
    @Max(value = 60, message = "La capacidad máxima es de 60 asientos")
    @Column(name = "capacidad", nullable = false)
    private Integer capacidad;

    @NotNull(message = "El año de fabricación es obligatorio")
    @Min(value = 2000, message = "El año de fabricación no puede ser anterior a 2000")
    @Max(value = 2030, message = "El año de fabricación no puede ser posterior a 2030")
    @Column(name = "anio_fabricacion", nullable = false)
    private Integer anioFabricacion;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    public String getDescripcion() {
        return placa + " – " + marca + " " + modelo + " (" + capacidad + " asientos)";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public Integer getAnioFabricacion() {
        return anioFabricacion;
    }

    public void setAnioFabricacion(Integer anioFabricacion) {
        this.anioFabricacion = anioFabricacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
