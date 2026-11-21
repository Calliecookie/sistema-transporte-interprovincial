package pe.transportes.gestion.servicio;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.transportes.gestion.modelo.Conductor;
import pe.transportes.gestion.repositorio.ConductorRepositorio;
import pe.transportes.gestion.repositorio.ViajeRepositorio;
import pe.transportes.gestion.util.Patrones;
import pe.transportes.gestion.util.RecursoNoEncontradoException;
import pe.transportes.gestion.util.ReglaNegocioException;
import pe.transportes.gestion.util.ValidadorEntidades;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class ServicioConductor {

    private final ConductorRepositorio repositorio;
    private final ViajeRepositorio viajeRepositorio;
    private final ValidadorEntidades validador;

    public ServicioConductor(ConductorRepositorio repositorio, ViajeRepositorio viajeRepositorio,
            ValidadorEntidades validador) {
        this.repositorio = repositorio;
        this.viajeRepositorio = viajeRepositorio;
        this.validador = validador;
    }

    @Transactional(readOnly = true)
    public Page<Conductor> buscar(String texto, Boolean activo, Pageable paginacion) {
        Specification<Conductor> especificacion = (raiz, consulta, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (Patrones.tieneTexto(texto)) {
                String patron = Patrones.contiene(texto);
                condiciones.add(cb.or(
                        cb.like(raiz.<String>get("dni"), patron, '\\'),
                        cb.like(cb.lower(raiz.<String>get("licencia")), patron, '\\'),
                        cb.like(cb.lower(raiz.<String>get("nombres")), patron, '\\'),
                        cb.like(cb.lower(raiz.<String>get("apellidos")), patron, '\\')));
            }
            if (activo != null) {
                condiciones.add(cb.equal(raiz.get("activo"), activo));
            }
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
        return repositorio.findAll(especificacion, paginacion);
    }

    @Transactional(readOnly = true)
    public List<Conductor> listarActivos() {
        return repositorio.findByActivoTrueOrderByApellidosAscNombresAsc();
    }

    @Transactional(readOnly = true)
    public Conductor obtener(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el conductor solicitado"));
    }

    @Transactional
    public Conductor crear(Conductor datos) {
        validador.validar(datos);
        String licencia = datos.getLicencia().toUpperCase(Locale.ROOT);
        verificarCategoria(datos);
        if (repositorio.existsByDni(datos.getDni())) {
            throw new ReglaNegocioException("dni", "Ya existe un conductor registrado con ese DNI");
        }
        if (repositorio.existsByLicencia(licencia)) {
            throw new ReglaNegocioException("licencia", "Ya existe un conductor registrado con esa licencia");
        }
        Conductor conductor = new Conductor();
        copiar(datos, conductor, licencia);
        return repositorio.save(conductor);
    }

    @Transactional
    public Conductor actualizar(Long id, Conductor datos) {
        validador.validar(datos);
        Conductor conductor = obtener(id);
        String licencia = datos.getLicencia().toUpperCase(Locale.ROOT);
        verificarCategoria(datos);
        if (repositorio.existsByDniAndIdNot(datos.getDni(), id)) {
            throw new ReglaNegocioException("dni", "Ya existe otro conductor registrado con ese DNI");
        }
        if (repositorio.existsByLicenciaAndIdNot(licencia, id)) {
            throw new ReglaNegocioException("licencia", "Ya existe otro conductor registrado con esa licencia");
        }
        copiar(datos, conductor, licencia);
        return repositorio.save(conductor);
    }

    @Transactional
    public void eliminar(Long id) {
        Conductor conductor = obtener(id);
        if (viajeRepositorio.existsByConductorId(id)) {
            throw new ReglaNegocioException("No se puede eliminar el conductor porque tiene viajes registrados");
        }
        repositorio.delete(conductor);
    }

    private void verificarCategoria(Conductor datos) {
        if (!Conductor.CATEGORIAS.contains(datos.getCategoriaLicencia())) {
            throw new ReglaNegocioException("categoriaLicencia", "Seleccione una categoría de licencia válida");
        }
    }

    private void copiar(Conductor datos, Conductor conductor, String licencia) {
        conductor.setDni(datos.getDni());
        conductor.setNombres(datos.getNombres());
        conductor.setApellidos(datos.getApellidos());
        conductor.setLicencia(licencia);
        conductor.setCategoriaLicencia(datos.getCategoriaLicencia());
        conductor.setTelefono(datos.getTelefono());
        conductor.setActivo(datos.isActivo());
    }
}
