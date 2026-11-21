package pe.transportes.gestion.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "destino")
public class Destino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del destino es obligatorio")
    @Size(max = 60, message = "El nombre no puede superar los 60 caracteres")
    @Column(name = "nombre", nullable = false, unique = true, length = 60)
    private String nombre;

    @NotBlank(message = "El departamento es obligatorio")
    @Size(max = 60, message = "El departamento no puede superar los 60 caracteres")
    @Column(name = "departamento", nullable = false, length = 60)
    private String departamento;

    @NotBlank(message = "El terminal es obligatorio")
    @Size(max = 100, message = "El terminal no puede superar los 100 caracteres")
    @Column(name = "terminal", nullable = false, length = 100)
    private String terminal;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getTerminal() {
        return terminal;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }
}
