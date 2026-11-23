package pe.transportes.gestion.integracion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ClienteFactiliza {

    private static final Logger REGISTRO = LoggerFactory.getLogger(ClienteFactiliza.class);

    private final PropiedadesFactiliza propiedades;
    private final ObjectMapper mapeador;
    private final HttpClient cliente;

    public ClienteFactiliza(PropiedadesFactiliza propiedades, ObjectMapper mapeador) {
        this.propiedades = propiedades;
        this.mapeador = mapeador;
        this.cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    public boolean estaConfigurado() {
        return propiedades.tieneToken();
    }

    public Optional<PersonaDni> consultar(String dni) {
        if (!propiedades.tieneToken()) {
            throw new ServicioNoDisponibleException("La consulta de DNI no está configurada");
        }
        HttpRequest solicitud = HttpRequest.newBuilder(URI.create(propiedades.urlBase() + "/dni/info/" + dni))
                .header("Authorization", "Bearer " + propiedades.token())
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(propiedades.segundosEspera()))
                .GET()
                .build();
        try {
            HttpResponse<String> respuesta = cliente.send(solicitud, HttpResponse.BodyHandlers.ofString());
            return interpretar(respuesta.statusCode(), respuesta.body(), dni);
        } catch (InterruptedException excepcion) {
            Thread.currentThread().interrupt();
            throw new ServicioNoDisponibleException("La consulta fue interrumpida");
        } catch (IOException excepcion) {
            REGISTRO.warn("No se pudo consultar Factiliza: {}", excepcion.getMessage());
            throw new ServicioNoDisponibleException("No hay conexión con el servicio de consulta de DNI");
        }
    }

    Optional<PersonaDni> interpretar(int estado, String cuerpo, String dni) throws IOException {
        if (estado == 400 || estado == 404) {
            return Optional.empty();
        }
        if (estado != 200) {
            REGISTRO.warn("Factiliza respondió con estado {}", estado);
            throw new ServicioNoDisponibleException("El servicio de consulta de DNI no respondió correctamente");
        }
        JsonNode raiz = mapeador.readTree(cuerpo);
        JsonNode datos = raiz.path("data");
        if (!raiz.path("success").asBoolean(false) || datos.isMissingNode() || datos.isNull()) {
            return Optional.empty();
        }
        String nombres = datos.path("nombres").asText("").strip();
        if (nombres.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new PersonaDni(dni, nombres, datos.path("apellido_paterno").asText("").strip(),
                datos.path("apellido_materno").asText("").strip()));
    }
}
