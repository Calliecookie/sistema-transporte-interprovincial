package pe.transportes.gestion.integracion;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "factiliza")
public record PropiedadesFactiliza(String urlBase, String token, Integer segundosEspera) {

    public PropiedadesFactiliza {
        if (urlBase == null || urlBase.isBlank()) {
            urlBase = "https://api.factiliza.com/v1";
        }
        if (segundosEspera == null || segundosEspera < 1) {
            segundosEspera = 8;
        }
    }

    public boolean tieneToken() {
        return token != null && !token.isBlank();
    }
}
