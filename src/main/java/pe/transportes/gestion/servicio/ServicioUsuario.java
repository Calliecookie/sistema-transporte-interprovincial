package pe.transportes.gestion.servicio;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.transportes.gestion.modelo.Conductor;
import pe.transportes.gestion.modelo.FormularioUsuario;
import pe.transportes.gestion.modelo.Rol;
import pe.transportes.gestion.modelo.Usuario;
import pe.transportes.gestion.repositorio.ConductorRepositorio;
import pe.transportes.gestion.repositorio.IncidenciaRepositorio;
import pe.transportes.gestion.repositorio.PasajeRepositorio;
import pe.transportes.gestion.repositorio.ReservaRepositorio;
import pe.transportes.gestion.repositorio.UsuarioRepositorio;
import pe.transportes.gestion.seguridad.UsuarioActual;
import pe.transportes.gestion.util.RecursoNoEncontradoException;
import pe.transportes.gestion.util.ReglaNegocioException;
import pe.transportes.gestion.util.ValidadorEntidades;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class ServicioUsuario {

    private final UsuarioRepositorio repositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final PasajeRepositorio pasajeRepositorio;
    private final PasswordEncoder codificador;
    private final UsuarioActual usuarioActual;
    private final ValidadorEntidades validador;
    private final ConductorRepositorio conductorRepositorio;
    private final IncidenciaRepositorio incidenciaRepositorio;

    public ServicioUsuario(UsuarioRepositorio repositorio, ReservaRepositorio reservaRepositorio,
            PasajeRepositorio pasajeRepositorio, PasswordEncoder codificador, UsuarioActual usuarioActual,
            ValidadorEntidades validador, ConductorRepositorio conductorRepositorio,
            IncidenciaRepositorio incidenciaRepositorio) {
        this.repositorio = repositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.pasajeRepositorio = pasajeRepositorio;
        this.codificador = codificador;
        this.usuarioActual = usuarioActual;
        this.validador = validador;
        this.conductorRepositorio = conductorRepositorio;
        this.incidenciaRepositorio = incidenciaRepositorio;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return repositorio.findAll(Sort.by("nombreCompleto"));
    }

    @Transactional(readOnly = true)
    public Usuario obtener(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario solicitado"));
    }

    @Transactional(readOnly = true)
    public FormularioUsuario formulario(Long id) {
        Usuario usuario = obtener(id);
        FormularioUsuario formulario = new FormularioUsuario();
        formulario.setNombreUsuario(usuario.getNombreUsuario());
        formulario.setNombreCompleto(usuario.getNombreCompleto());
        formulario.setRol(usuario.getRol());
        formulario.setActivo(usuario.isActivo());
        formulario.setIdConductor(usuario.getConductor() == null ? null : usuario.getConductor().getId());
        return formulario;
    }

    @Transactional
    public Usuario crear(FormularioUsuario formulario) {
        validador.validar(formulario);
        if (formulario.getContrasena() == null) {
            throw new ReglaNegocioException("contrasena", "La contraseña es obligatoria");
        }
        verificarConfirmacion(formulario);
        if (repositorio.existsByNombreUsuario(formulario.getNombreUsuario())) {
            throw new ReglaNegocioException("nombreUsuario", "Ya existe un usuario con ese nombre de usuario");
        }
        Usuario usuario = new Usuario();
        usuario.setConductor(resolverConductor(formulario, 0L));
        copiar(formulario, usuario);
        usuario.setContrasena(codificador.encode(formulario.getContrasena()));
        return repositorio.save(usuario);
    }

    @Transactional
    public Usuario actualizar(Long id, FormularioUsuario formulario) {
        validador.validar(formulario);
        Usuario usuario = obtener(id);
        if (formulario.getContrasena() != null) {
            verificarConfirmacion(formulario);
        }
        if (repositorio.existsByNombreUsuarioAndIdNot(formulario.getNombreUsuario(), id)) {
            throw new ReglaNegocioException("nombreUsuario", "Ya existe otro usuario con ese nombre de usuario");
        }
        if (esElUsuarioActual(id)
                && (formulario.getRol() != usuario.getRol() || !formulario.isActivo())) {
            throw new ReglaNegocioException("No puede cambiar el rol ni desactivar su propia cuenta");
        }
        usuario.setConductor(resolverConductor(formulario, id));
        copiar(formulario, usuario);
        if (formulario.getContrasena() != null) {
            usuario.setContrasena(codificador.encode(formulario.getContrasena()));
        }
        return repositorio.save(usuario);
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = obtener(id);
        if (esElUsuarioActual(id)) {
            throw new ReglaNegocioException("No puede eliminar su propia cuenta");
        }
        if (reservaRepositorio.existsByUsuarioId(id) || pasajeRepositorio.existsByUsuarioId(id)
                || incidenciaRepositorio.existsByUsuarioIdOrAtendidaPorId(id, id)) {
            throw new ReglaNegocioException(
                    "No se puede eliminar el usuario porque registró reservas o ventas. Puede desactivarlo en su lugar");
        }
        repositorio.delete(usuario);
    }

    private Conductor resolverConductor(FormularioUsuario formulario, Long id) {
        if (formulario.getRol() != Rol.CONDUCTOR) {
            return null;
        }
        if (formulario.getIdConductor() == null) {
            throw new ReglaNegocioException("idConductor", "Seleccione el conductor que usará esta cuenta");
        }
        Conductor conductor = conductorRepositorio.findById(formulario.getIdConductor())
                .orElseThrow(() -> new ReglaNegocioException("idConductor", "El conductor seleccionado no existe"));
        if (repositorio.existsByConductorIdAndIdNot(conductor.getId(), id)) {
            throw new ReglaNegocioException("idConductor", "Ese conductor ya tiene una cuenta de usuario");
        }
        return conductor;
    }

    private boolean esElUsuarioActual(Long id) {
        Optional<Usuario> actual = usuarioActual.obtener();
        return actual.isPresent() && actual.get().getId().equals(id);
    }

    private void verificarConfirmacion(FormularioUsuario formulario) {
        if (!formulario.getContrasena().equals(formulario.getConfirmacion())) {
            throw new ReglaNegocioException("confirmacion", "La confirmación no coincide con la contraseña");
        }
    }

    private void copiar(FormularioUsuario formulario, Usuario usuario) {
        usuario.setNombreUsuario(formulario.getNombreUsuario());
        usuario.setNombreCompleto(formulario.getNombreCompleto());
        usuario.setRol(formulario.getRol());
        usuario.setActivo(formulario.isActivo());
    }
}
