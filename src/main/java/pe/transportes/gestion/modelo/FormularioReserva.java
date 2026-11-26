package pe.transportes.gestion.modelo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class FormularioReserva {

    @NotNull(message = "Falta indicar el viaje")
    private Long viajeId;

    @NotNull(message = "Falta indicar el asiento")
    private Long asientoId;

    @NotBlank(message = "Ingrese el DNI del pasajero")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener exactamente 8 dígitos")
    private String dni;

    @NotNull(message = "Seleccione si desea reservar o vender")
    private AccionReserva accion = AccionReserva.RESERVAR;

    private MetodoPago metodoPago;

    private Pasajero pasajero = new Pasajero();

    public Pasajero getPasajero() {
        return pasajero;
    }

    public void setPasajero(Pasajero pasajero) {
        this.pasajero = pasajero;
    }

    public Long getViajeId() {
        return viajeId;
    }

    public void setViajeId(Long viajeId) {
        this.viajeId = viajeId;
    }

    public Long getAsientoId() {
        return asientoId;
    }

    public void setAsientoId(Long asientoId) {
        this.asientoId = asientoId;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public AccionReserva getAccion() {
        return accion;
    }

    public void setAccion(AccionReserva accion) {
        this.accion = accion;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }
}
