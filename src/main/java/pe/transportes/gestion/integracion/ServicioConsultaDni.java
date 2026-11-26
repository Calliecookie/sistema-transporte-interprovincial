package pe.transportes.gestion.integracion;

import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.transportes.gestion.modelo.Conductor;
import pe.transportes.gestion.modelo.Pasajero;
import pe.transportes.gestion.repositorio.ConductorRepositorio;
import pe.transportes.gestion.repositorio.PasajeroRepositorio;

@Service
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
public class ServicioConsultaDni {

    private final ClienteFactiliza clienteFactiliza;
    private final PasajeroRepositorio pasajeroRepositorio;
    private final ConductorRepositorio conductorRepositorio;

    public ServicioConsultaDni(ClienteFactiliza clienteFactiliza, PasajeroRepositorio pasajeroRepositorio,
            ConductorRepositorio conductorRepositorio) {
        this.clienteFactiliza = clienteFactiliza;
        this.pasajeroRepositorio = pasajeroRepositorio;
        this.conductorRepositorio = conductorRepositorio;
    }

    @Transactional(readOnly = true)
    public ResultadoConsultaDni consultarPasajero(String dni) {
        validar(dni);
        Optional<Pasajero> existente = pasajeroRepositorio.findByDni(dni);
        if (existente.isPresent()) {
            return ResultadoConsultaDni.yaRegistrado(existente.get(),
                    "Pasajero ya registrado: se usarán sus datos guardados");
        }
        return consultarExterno(dni);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public ResultadoConsultaDni consultarConductor(String dni) {
        validar(dni);
        Optional<Conductor> existente = conductorRepositorio.findByDni(dni);
        if (existente.isPresent()) {
            Conductor conductor = existente.get();
            return ResultadoConsultaDni.yaRegistrado(conductor.getNombres(), conductor.getApellidos(),
                    "Este DNI ya está registrado como conductor");
        }
        return consultarExterno(dni);
    }

    private ResultadoConsultaDni consultarExterno(String dni) {
        if (!clienteFactiliza.estaConfigurado()) {
            return ResultadoConsultaDni.sinResultado(
                    "La consulta automática no está configurada. Ingrese los datos manualmente");
        }
        try {
            return clienteFactiliza.consultar(dni)
                    .map(ResultadoConsultaDni::encontrado)
                    .orElseGet(() -> ResultadoConsultaDni.sinResultado("No se encontraron datos para ese DNI"));
        } catch (ServicioNoDisponibleException excepcion) {
            return ResultadoConsultaDni.sinResultado(excepcion.getMessage() + ". Ingrese los datos manualmente");
        }
    }

    private void validar(String dni) {
        if (dni == null || !dni.matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI debe tener exactamente 8 dígitos");
        }
    }
}
