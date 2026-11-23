package pe.transportes.gestion.integracion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClienteFactilizaPruebas {

    private ClienteFactiliza cliente;

    @BeforeEach
    void preparar() {
        cliente = new ClienteFactiliza(new PropiedadesFactiliza(null, "token", null), new ObjectMapper());
    }

    @Test
    void interpretaUnaRespuestaExitosa() throws Exception {
        String cuerpo = "{\"success\":true,\"data\":{\"numero\":\"12345678\",\"nombres\":\"ANA MARIA\","
                + "\"apellido_paterno\":\"PEREZ\",\"apellido_materno\":\"QUISPE\"}}";

        Optional<PersonaDni> persona = cliente.interpretar(200, cuerpo, "12345678");

        assertThat(persona).isPresent();
        assertThat(persona.get().nombres()).isEqualTo("ANA MARIA");
        assertThat(persona.get().apellidos()).isEqualTo("PEREZ QUISPE");
    }

    @Test
    void devuelveVacioCuandoNoExisteElDni() throws Exception {
        assertThat(cliente.interpretar(404, "", "12345678")).isEmpty();
        assertThat(cliente.interpretar(200, "{\"success\":false}", "12345678")).isEmpty();
    }

    @Test
    void fallaCuandoElServicioRespondeConError() {
        assertThatThrownBy(() -> cliente.interpretar(500, "", "12345678"))
                .isInstanceOf(ServicioNoDisponibleException.class);
    }

    @Test
    void sinTokenNoConsulta() {
        ClienteFactiliza sinToken = new ClienteFactiliza(new PropiedadesFactiliza(null, "", null), new ObjectMapper());

        assertThat(sinToken.estaConfigurado()).isFalse();
        assertThatThrownBy(() -> sinToken.consultar("12345678")).isInstanceOf(ServicioNoDisponibleException.class);
    }
}
