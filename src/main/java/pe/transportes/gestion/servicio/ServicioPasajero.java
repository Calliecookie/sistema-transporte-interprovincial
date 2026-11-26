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
import pe.transportes.gestion.modelo.Pasajero;
import pe.transportes.gestion.repositorio.PasajeroRepositorio;
import pe.transportes.gestion.repositorio.ReservaRepositorio;
import pe.transportes.gestion.util.Patrones;
import pe.transportes.gestion.util.RecursoNoEncontradoException;
import pe.transportes.gestion.util.ReglaNegocioException;
import pe.transportes.gestion.util.ValidadorEntidades;

@Service
@PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
public class ServicioPasajero {

    private final PasajeroRepositorio repositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final ValidadorEntidades validador;

    public ServicioPasajero(PasajeroRepositorio repositorio, ReservaRepositorio reservaRepositorio,
            ValidadorEntidades validador) {
        this.repositorio = repositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.validador = validador;
    }

    @Transactional(readOnly = true)
    public Page<Pasajero> buscar(String dni, String texto, Pageable paginacion) {
        Specification<Pasajero> especificacion = (raiz, consulta, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (Patrones.tieneTexto(dni)) {
                condiciones.add(cb.like(raiz.<String>get("dni"), Patrones.contiene(dni), '\\'));
            }
            if (Patrones.tieneTexto(texto)) {
                String patron = Patrones.contiene(texto);
                condiciones.add(cb.or(
                        cb.like(cb.lower(raiz.<String>get("nombres")), patron, '\\'),
                        cb.like(cb.lower(raiz.<String>get("apellidos")), patron, '\\')));
            }
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
        return repositorio.findAll(especificacion, paginacion);
    }

    @Transactional(readOnly = true)
    public Pasajero obtener(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el pasajero solicitado"));
    }

    @Transactional
    public Pasajero crear(Pasajero datos) {
        validador.validar(datos);
        if (repositorio.existsByDni(datos.getDni())) {
            throw new ReglaNegocioException("dni", "Ya existe un pasajero registrado con ese DNI");
        }
        Pasajero pasajero = new Pasajero();
        copiar(datos, pasajero);
        return repositorio.save(pasajero);
    }

    @Transactional
    public Pasajero actualizar(Long id, Pasajero datos) {
        validador.validar(datos);
        Pasajero pasajero = obtener(id);
        if (repositorio.existsByDniAndIdNot(datos.getDni(), id)) {
            throw new ReglaNegocioException("dni", "Ya existe otro pasajero registrado con ese DNI");
        }
        copiar(datos, pasajero);
        return repositorio.save(pasajero);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void eliminar(Long id) {
        Pasajero pasajero = obtener(id);
        if (reservaRepositorio.existsByPasajeroId(id)) {
            throw new ReglaNegocioException("No se puede eliminar el pasajero porque tiene reservas registradas");
        }
        repositorio.delete(pasajero);
    }

    private void copiar(Pasajero datos, Pasajero pasajero) {
        pasajero.setDni(datos.getDni());
        pasajero.setNombres(datos.getNombres());
        pasajero.setApellidoPaterno(datos.getApellidoPaterno());
        pasajero.setApellidoMaterno(datos.getApellidoMaterno());
        pasajero.setApellidos(datos.getApellidos());
        pasajero.setDepartamento(datos.getDepartamento());
        pasajero.setProvincia(datos.getProvincia());
        pasajero.setDistrito(datos.getDistrito());
        pasajero.setDireccion(datos.getDireccion());
        pasajero.setDireccionCompleta(datos.getDireccionCompleta());
        pasajero.setSexo(datos.getSexo());
        pasajero.setTelefono(datos.getTelefono());
        pasajero.setCorreo(datos.getCorreo());
        pasajero.setFechaNacimiento(datos.getFechaNacimiento());
    }
}
