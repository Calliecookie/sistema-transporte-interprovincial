package pe.transportes.gestion.servicio;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.transportes.gestion.modelo.Destino;
import pe.transportes.gestion.modelo.Ruta;
import pe.transportes.gestion.repositorio.DestinoRepositorio;
import pe.transportes.gestion.repositorio.RutaRepositorio;
import pe.transportes.gestion.repositorio.ViajeRepositorio;
import pe.transportes.gestion.util.RecursoNoEncontradoException;
import pe.transportes.gestion.util.ReglaNegocioException;
import pe.transportes.gestion.util.ValidadorEntidades;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class ServicioRuta {

    private final RutaRepositorio repositorio;
    private final DestinoRepositorio destinoRepositorio;
    private final ViajeRepositorio viajeRepositorio;
    private final ValidadorEntidades validador;

    public ServicioRuta(RutaRepositorio repositorio, DestinoRepositorio destinoRepositorio,
            ViajeRepositorio viajeRepositorio, ValidadorEntidades validador) {
        this.repositorio = repositorio;
        this.destinoRepositorio = destinoRepositorio;
        this.viajeRepositorio = viajeRepositorio;
        this.validador = validador;
    }

    @Transactional(readOnly = true)
    public Page<Ruta> buscar(Long origenId, Long destinoId, Boolean activa, Pageable paginacion) {
        Specification<Ruta> especificacion = (raiz, consulta, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (origenId != null) {
                condiciones.add(cb.equal(raiz.get("origen").get("id"), origenId));
            }
            if (destinoId != null) {
                condiciones.add(cb.equal(raiz.get("destino").get("id"), destinoId));
            }
            if (activa != null) {
                condiciones.add(cb.equal(raiz.get("activa"), activa));
            }
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
        return repositorio.findAll(especificacion, paginacion);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @Transactional(readOnly = true)
    public List<Ruta> listar() {
        return repositorio.findAllByOrderByOrigenNombreAscDestinoNombreAsc();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @Transactional(readOnly = true)
    public List<Ruta> listarActivas() {
        return repositorio.findByActivaTrueOrderByOrigenNombreAscDestinoNombreAsc();
    }

    @Transactional(readOnly = true)
    public Ruta obtener(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la ruta solicitada"));
    }

    @Transactional
    public Ruta crear(Ruta datos) {
        validador.validar(datos);
        Destino origen = buscarDestino(datos.getOrigen(), "idOrigen", "origen");
        Destino destino = buscarDestino(datos.getDestino(), "idDestino", "destino");
        verificarExtremos(origen, destino);
        if (repositorio.existsByOrigenIdAndDestinoId(origen.getId(), destino.getId())) {
            throw new ReglaNegocioException("idDestino", "Ya existe una ruta registrada entre esos dos destinos");
        }
        Ruta ruta = new Ruta();
        copiar(datos, ruta, origen, destino);
        return repositorio.save(ruta);
    }

    @Transactional
    public Ruta actualizar(Long id, Ruta datos) {
        validador.validar(datos);
        Ruta ruta = obtener(id);
        Destino origen = buscarDestino(datos.getOrigen(), "idOrigen", "origen");
        Destino destino = buscarDestino(datos.getDestino(), "idDestino", "destino");
        verificarExtremos(origen, destino);
        if (repositorio.existsByOrigenIdAndDestinoIdAndIdNot(origen.getId(), destino.getId(), id)) {
            throw new ReglaNegocioException("idDestino", "Ya existe otra ruta registrada entre esos dos destinos");
        }
        boolean cambioExtremos = !ruta.getOrigen().getId().equals(origen.getId())
                || !ruta.getDestino().getId().equals(destino.getId());
        if (cambioExtremos && viajeRepositorio.existsByRutaId(id)) {
            throw new ReglaNegocioException("idDestino",
                    "No se puede cambiar el origen ni el destino de una ruta con viajes registrados");
        }
        copiar(datos, ruta, origen, destino);
        return repositorio.save(ruta);
    }

    @Transactional
    public void eliminar(Long id) {
        Ruta ruta = obtener(id);
        if (viajeRepositorio.existsByRutaId(id)) {
            throw new ReglaNegocioException("No se puede eliminar la ruta porque tiene viajes registrados");
        }
        repositorio.delete(ruta);
    }

    private Destino buscarDestino(Destino referencia, String campo, String nombre) {
        if (referencia == null || referencia.getId() == null) {
            throw new ReglaNegocioException(campo, "Seleccione el " + nombre);
        }
        return destinoRepositorio.findById(referencia.getId())
                .orElseThrow(() -> new ReglaNegocioException(campo, "El " + nombre + " seleccionado no existe"));
    }

    private void verificarExtremos(Destino origen, Destino destino) {
        if (origen.getId().equals(destino.getId())) {
            throw new ReglaNegocioException("idDestino", "El destino debe ser distinto del origen");
        }
    }

    private void copiar(Ruta datos, Ruta ruta, Destino origen, Destino destino) {
        ruta.setOrigen(origen);
        ruta.setDestino(destino);
        ruta.setDistanciaKm(datos.getDistanciaKm());
        ruta.setDuracionHoras(datos.getDuracionHoras());
        ruta.setPrecioBase(datos.getPrecioBase());
        ruta.setActiva(datos.isActiva());
    }
}
