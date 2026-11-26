package pe.transportes.gestion.recuperacion;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aplicacion")
public record PropiedadesAplicacion(String urlPublica, String correoRemitente, Integer minutosVigenciaEnlace) {

    public PropiedadesAplicacion {
        if (urlPublica == null || urlPublica.isBlank()) {
            urlPublica = "http://localhost:8081";
        }
        if (urlPublica.endsWith("/")) {
            urlPublica = urlPublica.substring(0, urlPublica.length() - 1);
        }
        if (minutosVigenciaEnlace == null || minutosVigenciaEnlace < 5) {
            minutosVigenciaEnlace = 30;
        }
    }
}
