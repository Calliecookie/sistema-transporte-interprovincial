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
import pe.transportes.gestion.modelo.Bus;
import pe.transportes.gestion.repositorio.BusRepositorio;
import pe.transportes.gestion.repositorio.ViajeRepositorio;
import pe.transportes.gestion.util.Patrones;
import pe.transportes.gestion.util.RecursoNoEncontradoException;
import pe.transportes.gestion.util.ReglaNegocioException;
import pe.transportes.gestion.util.ValidadorEntidades;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class ServicioBus {

    private final BusRepositorio repositorio;
    private final ViajeRepositorio viajeRepositorio;
    private final ValidadorEntidades validador;

    public ServicioBus(BusRepositorio repositorio, ViajeRepositorio viajeRepositorio, ValidadorEntidades validador) {
        this.repositorio = repositorio;
        this.viajeRepositorio = viajeRepositorio;
        this.validador = validador;
    }

    @Transactional(readOnly = true)
    public Page<Bus> buscar(String texto, Boolean activo, Pageable paginacion) {
        Specification<Bus> especificacion = (raiz, consulta, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (Patrones.tieneTexto(texto)) {
                String patron = Patrones.contiene(texto);
                condiciones.add(cb.or(
                        cb.like(cb.lower(raiz.<String>get("placa")), patron, '\\'),
                        cb.like(cb.lower(raiz.<String>get("marca")), patron, '\\'),
                        cb.like(cb.lower(raiz.<String>get("modelo")), patron, '\\')));
            }
            if (activo != null) {
                condiciones.add(cb.equal(raiz.get("activo"), activo));
            }
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
        return repositorio.findAll(especificacion, paginacion);
    }

    @Transactional(readOnly = true)
    public List<Bus> listarActivos() {
        return repositorio.findByActivoTrueOrderByPlacaAsc();
    }

    @Transactional(readOnly = true)
    public Bus obtener(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el bus solicitado"));
    }

    @Transactional
    public Bus crear(Bus datos) {
        validador.validar(datos);
        String placa = datos.getPlaca().toUpperCase(Locale.ROOT);
        if (repositorio.existsByPlaca(placa)) {
            throw new ReglaNegocioException("placa", "Ya existe un bus registrado con esa placa");
        }
        Bus bus = new Bus();
        copiar(datos, bus, placa);
        return repositorio.save(bus);
    }

    @Transactional
    public Bus actualizar(Long id, Bus datos) {
        validador.validar(datos);
        Bus bus = obtener(id);
        String placa = datos.getPlaca().toUpperCase(Locale.ROOT);
        if (repositorio.existsByPlacaAndIdNot(placa, id)) {
            throw new ReglaNegocioException("placa", "Ya existe otro bus registrado con esa placa");
        }
        if (!bus.getCapacidad().equals(datos.getCapacidad()) && viajeRepositorio.existsByBusId(id)) {
            throw new ReglaNegocioException("capacidad",
                    "No se puede cambiar la capacidad de un bus que ya tiene viajes registrados");
        }
        copiar(datos, bus, placa);
        return repositorio.save(bus);
    }

    @Transactional
    public void eliminar(Long id) {
        Bus bus = obtener(id);
        if (viajeRepositorio.existsByBusId(id)) {
            throw new ReglaNegocioException("No se puede eliminar el bus porque tiene viajes registrados");
        }
        repositorio.delete(bus);
    }

    private void copiar(Bus datos, Bus bus, String placa) {
        bus.setPlaca(placa);
        bus.setMarca(datos.getMarca());
        bus.setModelo(datos.getModelo());
        bus.setCapacidad(datos.getCapacidad());
        bus.setAnioFabricacion(datos.getAnioFabricacion());
        bus.setActivo(datos.isActivo());
    }
}
