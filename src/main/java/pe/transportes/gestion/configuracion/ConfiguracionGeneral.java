package pe.transportes.gestion.configuracion;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionGeneral {

    public static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    @Bean
    public Clock reloj() {
        return Clock.system(ZONA_LIMA);
    }
}
