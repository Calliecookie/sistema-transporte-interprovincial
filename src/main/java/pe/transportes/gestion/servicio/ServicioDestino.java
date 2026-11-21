package pe.transportes.gestion.servicio;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.transportes.gestion.modelo.Destino;
import pe.transportes.gestion.repositorio.DestinoRepositorio;
import pe.transportes.gestion.repositorio.RutaRepositorio;
import pe.transportes.gestion.util.Patrones;
import pe.transportes.gestion.util.RecursoNoEncontradoException;
import pe.transportes.gestion.util.ReglaNegocioException;
import pe.transportes.gestion.util.ValidadorEntidades;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class ServicioDestino {

    private final DestinoRepositorio repositorio;
    private final RutaRepositorio rutaRepositorio;
    private final ValidadorEntidades validador;

    public ServicioDestino(DestinoRepositorio repositorio, RutaRepositorio rutaRepositorio,
            ValidadorEntidades validador) {
        this.repositorio = repositorio;
        this.rutaRepositorio = rutaRepositorio;
        this.validador = validador;
    }

    @Transactional(readOnly = true)
    public List<Destino> listar() {
        return repositorio.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public Page<Destino> buscar(String texto, Pageable paginacion) {
        Specification<Destino> especificacion = (raiz, consulta, cb) -> {
            if (!Patrones.tieneTexto(texto)) {
                return cb.conjunction();
            }
            String patron = Patrones.contiene(texto);
            return cb.or(
                    cb.like(cb.lower(raiz.<String>get("nombre")), patron, '\\'),
                    cb.like(cb.lower(raiz.<String>get("departamento")), patron, '\\'),
                    cb.like(cb.lower(raiz.<String>get("terminal")), patron, '\\'));
        };
        return repositorio.findAll(especificacion, paginacion);
    }

    @Transactional(readOnly = true)
    public Destino obtener(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el destino solicitado"));
    }

    @Transactional
    public Destino crear(Destino datos) {
        validador.validar(datos);
        if (repositorio.existsByNombreIgnoreCase(datos.getNombre())) {
            throw new ReglaNegocioException("nombre", "Ya existe un destino con ese nombre");
        }
        Destino destino = new Destino();
        copiar(datos, destino);
        return repositorio.save(destino);
    }

    @Transactional
    public Destino actualizar(Long id, Destino datos) {
        validador.validar(datos);
        Destino destino = obtener(id);
        if (repositorio.existsByNombreIgnoreCaseAndIdNot(datos.getNombre(), id)) {
            throw new ReglaNegocioException("nombre", "Ya existe otro destino con ese nombre");
        }
        copiar(datos, destino);
        return repositorio.save(destino);
    }

    @Transactional
    public void eliminar(Long id) {
        Destino destino = obtener(id);
        if (rutaRepositorio.existsByOrigenIdOrDestinoId(id, id)) {
            throw new ReglaNegocioException("No se puede eliminar el destino porque tiene rutas registradas");
        }
        repositorio.delete(destino);
    }

    private void copiar(Destino origen, Destino destino) {
        destino.setNombre(origen.getNombre());
        destino.setDepartamento(origen.getDepartamento());
        destino.setTerminal(origen.getTerminal());
    }
}
