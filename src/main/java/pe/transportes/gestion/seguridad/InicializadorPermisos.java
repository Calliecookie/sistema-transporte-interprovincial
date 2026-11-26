package pe.transportes.gestion.seguridad;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import pe.transportes.gestion.modelo.Modulo;
import pe.transportes.gestion.modelo.PermisoRol;
import pe.transportes.gestion.repositorio.PermisoRolRepositorio;

@Component
public class InicializadorPermisos implements ApplicationRunner {

    private final PermisoRolRepositorio repositorio;

    public InicializadorPermisos(PermisoRolRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void run(ApplicationArguments argumentos) {
        if (repositorio.count() > 0) {
            return;
        }
        for (Modulo modulo : Modulo.values()) {
            repositorio.save(new PermisoRol(modulo.getRolAplicable(), modulo));
        }
    }
}
