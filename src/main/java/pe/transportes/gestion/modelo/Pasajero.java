package pe.transportes.gestion.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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

    @NotBlank(message = "El apellido paterno es obligatorio")
    @Size(max = 39, message = "El apellido paterno no puede superar los 39 caracteres")
    @Pattern(regexp = "[\\p{L} '.-]*", message = "El apellido paterno solo puede contener letras y espacios")
    @Column(name = "apellido_paterno", length = 39)
    private String apellidoPaterno;

    @Size(max = 39, message = "El apellido materno no puede superar los 39 caracteres")
    @Pattern(regexp = "[\\p{L} '.-]*", message = "El apellido materno solo puede contener letras y espacios")
    @Column(name = "apellido_materno", length = 39)
    private String apellidoMaterno;

    @Size(max = 80, message = "Los apellidos no pueden superar los 80 caracteres")
    @Column(name = "apellidos", nullable = false, length = 80)
    private String apellidos;

    @Size(max = 60, message = "El departamento no puede superar los 60 caracteres")
    @Column(name = "departamento", length = 60)
    private String departamento;

    @Size(max = 60, message = "La provincia no puede superar los 60 caracteres")
    @Column(name = "provincia", length = 60)
    private String provincia;

    @Size(max = 60, message = "El distrito no puede superar los 60 caracteres")
    @Column(name = "distrito", length = 60)
    private String distrito;

    @Size(max = 150, message = "La dirección no puede superar los 150 caracteres")
    @Column(name = "direccion", length = 150)
    private String direccion;

    @Size(max = 250, message = "La dirección completa no puede superar los 250 caracteres")
    @Column(name = "direccion_completa", length = 250)
    private String direccionCompleta;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "sexo", length = 10)
    private Sexo sexo;

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

    public String getApellidoPaterno() {
        if (apellidoPaterno == null && apellidoMaterno == null && apellidos != null && !apellidos.isBlank()) {
            String[] partes = apellidos.strip().split("\\s+", 2);
            return partes[0];
        }
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
        recomponerApellidos();
    }

    public String getApellidoMaterno() {
        if (apellidoPaterno == null && apellidoMaterno == null && apellidos != null && !apellidos.isBlank()) {
            String[] partes = apellidos.strip().split("\\s+", 2);
            return partes.length > 1 ? partes[1] : null;
        }
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
        recomponerApellidos();
    }

    private void recomponerApellidos() {
        StringBuilder compuesto = new StringBuilder();
        if (apellidoPaterno != null && !apellidoPaterno.isBlank()) {
            compuesto.append(apellidoPaterno.strip());
        }
        if (apellidoMaterno != null && !apellidoMaterno.isBlank()) {
            if (compuesto.length() > 0) {
                compuesto.append(' ');
            }
            compuesto.append(apellidoMaterno.strip());
        }
        this.apellidos = compuesto.length() == 0 ? null : compuesto.toString();
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDireccionCompleta() {
        return direccionCompleta;
    }

    public void setDireccionCompleta(String direccionCompleta) {
        this.direccionCompleta = direccionCompleta;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
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
