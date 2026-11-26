package pe.transportes.gestion.configuracion;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.transportes.gestion.integracion.PropiedadesFactiliza;
import pe.transportes.gestion.recuperacion.PropiedadesAplicacion;

@Configuration
@EnableConfigurationProperties({PropiedadesFactiliza.class, PropiedadesAplicacion.class})
public class ConfiguracionGeneral {

    public static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    @Bean
    public Clock reloj() {
        return Clock.system(ZONA_LIMA);
    }
}
