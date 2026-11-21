package pe.transportes.gestion.util;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class ExportadorCsv {

    private static final byte[] MARCA_UTF8 = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final String SEPARADOR = ",";
    private static final String SALTO = "\r\n";

    private ExportadorCsv() {
    }

    public static byte[] generar(List<String> cabeceras, List<List<String>> filas) {
        StringBuilder contenido = new StringBuilder();
        agregarFila(contenido, cabeceras);
        for (List<String> fila : filas) {
            agregarFila(contenido, fila);
        }
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        salida.writeBytes(MARCA_UTF8);
        salida.writeBytes(contenido.toString().getBytes(StandardCharsets.UTF_8));
        return salida.toByteArray();
    }

    static String celda(String valor) {
        String texto = valor == null ? "" : valor;
        if (!texto.isEmpty() && "=+-@\t\r".indexOf(texto.charAt(0)) >= 0) {
            texto = "'" + texto;
        }
        boolean requiereComillas = texto.contains(SEPARADOR) || texto.contains("\"") || texto.contains("\n")
                || texto.contains("\r");
        if (requiereComillas) {
            return "\"" + texto.replace("\"", "\"\"") + "\"";
        }
        return texto;
    }

    private static void agregarFila(StringBuilder contenido, List<String> fila) {
        for (int i = 0; i < fila.size(); i++) {
            if (i > 0) {
                contenido.append(SEPARADOR);
            }
            contenido.append(celda(fila.get(i)));
        }
        contenido.append(SALTO);
    }
}
