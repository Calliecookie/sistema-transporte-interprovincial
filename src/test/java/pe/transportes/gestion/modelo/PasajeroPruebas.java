package pe.transportes.gestion.modelo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PasajeroPruebas {

    @Test
    void componeLosApellidosConElPaternoYElMaterno() {
        Pasajero pasajero = new Pasajero();
        pasajero.setApellidoPaterno("Castillo");
        pasajero.setApellidoMaterno("Terrones");

        assertThat(pasajero.getApellidos()).isEqualTo("Castillo Terrones");
    }

    @Test
    void admiteUnSoloApellido() {
        Pasajero pasajero = new Pasajero();
        pasajero.setApellidoPaterno("Castillo");
        pasajero.setApellidoMaterno(null);

        assertThat(pasajero.getApellidos()).isEqualTo("Castillo");
    }

    @Test
    void separaLosApellidosDeRegistrosAntiguos() {
        Pasajero pasajero = new Pasajero();
        pasajero.setApellidos("Quispe Huamán");

        assertThat(pasajero.getApellidoPaterno()).isEqualTo("Quispe");
        assertThat(pasajero.getApellidoMaterno()).isEqualTo("Huamán");
    }

    @Test
    void interpretaElSexoEnVariosFormatos() {
        assertThat(Sexo.desde("M")).isEqualTo(Sexo.MASCULINO);
        assertThat(Sexo.desde("femenino")).isEqualTo(Sexo.FEMENINO);
        assertThat(Sexo.desde("2")).isEqualTo(Sexo.FEMENINO);
        assertThat(Sexo.desde("")).isNull();
        assertThat(Sexo.desde(null)).isNull();
    }
}
