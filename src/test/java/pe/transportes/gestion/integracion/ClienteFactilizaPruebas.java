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
    void leeLaFechaDeNacimientoCuandoLaRespuestaLaIncluye() throws Exception {
        String cuerpo = "{\"success\":true,\"data\":{\"nombres\":\"ANA\",\"apellido_paterno\":\"PEREZ\","
                + "\"apellido_materno\":\"QUISPE\",\"fecha_nacimiento\":\"15/03/1990\"}}";

        assertThat(cliente.interpretar(200, cuerpo, "12345678").get().fechaNacimiento())
                .isEqualTo(java.time.LocalDate.of(1990, 3, 15));
        assertThat(ClienteFactiliza.leerFecha("1990-03-15")).isEqualTo(java.time.LocalDate.of(1990, 3, 15));
        assertThat(ClienteFactiliza.leerFecha("")).isNull();
        assertThat(ClienteFactiliza.leerFecha("fecha rara")).isNull();
    }

    @Test
    void leeUbicacionDireccionYSexo() throws Exception {
        String cuerpo = "{\"success\":true,\"data\":{\"nombres\":\"JOSE PEDRO\",\"apellido_paterno\":\"CASTILLO\","
                + "\"apellido_materno\":\"TERRONES\",\"departamento\":\"CAJAMARCA\",\"provincia\":\"CHOTA\","
                + "\"distrito\":\"TACABAMBA\",\"direccion\":\"CASERIO PUÑA\","
                + "\"direccion_completa\":\"CASERIO PUÑA, CAJAMARCA - CHOTA - TACABAMBA\",\"sexo\":\"M\"}}";

        PersonaDni persona = cliente.interpretar(200, cuerpo, "27427864").get();

        assertThat(persona.departamento()).isEqualTo("CAJAMARCA");
        assertThat(persona.provincia()).isEqualTo("CHOTA");
        assertThat(persona.distrito()).isEqualTo("TACABAMBA");
        assertThat(persona.direccion()).isEqualTo("CASERIO PUÑA");
        assertThat(persona.direccionCompleta()).contains("TACABAMBA");
        assertThat(persona.sexo()).isEqualTo(pe.transportes.gestion.modelo.Sexo.MASCULINO);
        assertThat(persona.fechaNacimiento()).isNull();
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
