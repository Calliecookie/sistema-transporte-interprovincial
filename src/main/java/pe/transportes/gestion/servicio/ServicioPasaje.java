package pe.transportes.gestion.servicio;

import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.transportes.gestion.modelo.EstadoPasaje;
import pe.transportes.gestion.modelo.Pasaje;
import pe.transportes.gestion.repositorio.EspecificacionPasaje;
import pe.transportes.gestion.repositorio.PasajeRepositorio;
import pe.transportes.gestion.util.RangoFechas;
import pe.transportes.gestion.util.RecursoNoEncontradoException;

@Service
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
public class ServicioPasaje {

    private final PasajeRepositorio repositorio;

    public ServicioPasaje(PasajeRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public Page<Pasaje> buscar(String dni, String codigo, EstadoPasaje estado, LocalDate desde, LocalDate hasta,
            Pageable paginacion) {
        RangoFechas.validar(desde, hasta);
        return repositorio.findAll(EspecificacionPasaje.filtrar(dni, codigo, estado, desde, hasta), paginacion);
    }

    @Transactional(readOnly = true)
    public Pasaje obtener(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el pasaje solicitado"));
    }
}
