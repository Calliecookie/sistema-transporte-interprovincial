package pe.transportes.gestion.seguridad;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.transportes.gestion.repositorio.UsuarioRepositorio;

@Service
public class ServicioUsuarioDetalle implements UserDetailsService {

    private final UsuarioRepositorio repositorio;

    public ServicioUsuarioDetalle(UsuarioRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String nombreUsuario) throws UsernameNotFoundException {
        return repositorio.findByNombreUsuario(nombreUsuario)
                .map(UsuarioDetalle::new)
                .orElseThrow(() -> new UsernameNotFoundException("No existe el usuario indicado"));
    }
}
