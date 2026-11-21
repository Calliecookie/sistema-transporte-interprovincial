package pe.transportes.gestion.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name = "conductor")
public class Conductor {

    public static final List<String> CATEGORIAS = List.of("A-IIb", "A-IIIa", "A-IIIb", "A-IIIc");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener exactamente 8 dígitos")
    @Column(name = "dni", nullable = false, unique = true, length = 8)
    private String dni;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 80, message = "Los nombres no pueden superar los 80 caracteres")
    @Pattern(regexp = "[\\p{L} '.-]*", message = "Los nombres solo pueden contener letras y espacios")
    @Column(name = "nombres", nullable = false, length = 80)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 80, message = "Los apellidos no pueden superar los 80 caracteres")
    @Pattern(regexp = "[\\p{L} '.-]*", message = "Los apellidos solo pueden contener letras y espacios")
    @Column(name = "apellidos", nullable = false, length = 80)
    private String apellidos;

    @NotBlank(message = "La licencia de conducir es obligatoria")
    @Pattern(regexp = "[A-Za-z]\\d{8}", message = "La licencia debe tener una letra seguida de 8 dígitos (ejemplo: Q12345678)")
    @Column(name = "licencia", nullable = false, unique = true, length = 9)
    private String licencia;

    @NotBlank(message = "Seleccione la categoría de la licencia")
    @Size(max = 10, message = "La categoría no puede superar los 10 caracteres")
    @Column(name = "categoria_licencia", nullable = false, length = 10)
    private String categoriaLicencia;

    @Pattern(regexp = "9\\d{8}", message = "El teléfono debe tener 9 dígitos y empezar con 9")
    @Column(name = "telefono", length = 9)
    private String telefono;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
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

    public String getLicencia() {
        return licencia;
    }

    public void setLicencia(String licencia) {
        this.licencia = licencia;
    }

    public String getCategoriaLicencia() {
        return categoriaLicencia;
    }

    public void setCategoriaLicencia(String categoriaLicencia) {
        this.categoriaLicencia = categoriaLicencia;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
