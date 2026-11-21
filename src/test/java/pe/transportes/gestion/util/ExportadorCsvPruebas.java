package pe.transportes.gestion.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExportadorCsvPruebas {

    @Test
    void generaCabecerasFilasYMarcaUtf8() {
        byte[] contenido = ExportadorCsv.generar(List.of("DNI", "Pasajero"),
                List.of(List.of("45218790", "Lucía Rojas")));

        assertThat(Arrays.copyOf(contenido, 3)).containsExactly((byte) 0xEF, (byte) 0xBB, (byte) 0xBF);
        String texto = new String(contenido, 3, contenido.length - 3, StandardCharsets.UTF_8);
        assertThat(texto).isEqualTo("DNI,Pasajero\r\n45218790,Lucía Rojas\r\n");
    }

    @Test
    void encierraEntreComillasLosValoresConComasYComillas() {
        assertThat(ExportadorCsv.celda("Quispe, Carlos")).isEqualTo("\"Quispe, Carlos\"");
        assertThat(ExportadorCsv.celda("Dijo \"hola\"")).isEqualTo("\"Dijo \"\"hola\"\"\"");
    }

    @Test
    void neutralizaLasCeldasQueEmpiezanComoFormula() {
        assertThat(ExportadorCsv.celda("=SUMA(A1:A2)")).isEqualTo("'=SUMA(A1:A2)");
        assertThat(ExportadorCsv.celda("+51999")).isEqualTo("'+51999");
    }

    @Test
    void tratalosNulosComoTextoVacio() {
        assertThat(ExportadorCsv.celda(null)).isEmpty();
    }
}
