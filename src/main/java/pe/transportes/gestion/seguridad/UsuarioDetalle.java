package pe.transportes.gestion.seguridad;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pe.transportes.gestion.modelo.Rol;
import pe.transportes.gestion.modelo.Usuario;

public class UsuarioDetalle implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String nombreUsuario;
    private final String contrasena;
    private final String nombreCompleto;
    private final Rol rol;
    private final boolean activo;

    public UsuarioDetalle(Usuario usuario) {
        this.id = usuario.getId();
        this.nombreUsuario = usuario.getNombreUsuario();
        this.contrasena = usuario.getContrasena();
        this.nombreCompleto = usuario.getNombreCompleto();
        this.rol = usuario.getRol();
        this.activo = usuario.isActivo();
    }

    public Long getId() {
        return id;
    }

    public Rol getRol() {
        return rol;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getEtiquetaRol() {
        return rol.getEtiqueta();
    }

    public String getInicial() {
        return nombreCompleto.substring(0, 1).toUpperCase();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() {
        return contrasena;
    }

    @Override
    public String getUsername() {
        return nombreUsuario;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}
