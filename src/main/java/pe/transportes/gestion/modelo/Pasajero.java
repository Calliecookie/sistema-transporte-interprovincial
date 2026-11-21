package pe.transportes.gestion.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "pasajero")
public class Pasajero {

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

    @Pattern(regexp = "9\\d{8}", message = "El teléfono debe tener 9 dígitos y empezar con 9")
    @Column(name = "telefono", length = 9)
    private String telefono;

    @Email(message = "Ingrese un correo electrónico válido")
    @Size(max = 100, message = "El correo no puede superar los 100 caracteres")
    @Column(name = "correo", length = 100)
    private String correo;

    @Past(message = "La fecha de nacimiento debe ser anterior a la fecha actual")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
}
