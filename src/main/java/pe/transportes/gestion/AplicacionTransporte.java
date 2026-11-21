package pe.transportes.gestion;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pe.transportes.gestion.configuracion.ConfiguracionGeneral;

@SpringBootApplication
public class AplicacionTransporte {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(ConfiguracionGeneral.ZONA_LIMA));
        SpringApplication.run(AplicacionTransporte.class, args);
    }
}
