package pe.transportes.gestion.util;

import org.springframework.web.util.UriComponentsBuilder;

public final class ParametrosUrl {

    private ParametrosUrl() {
    }

    public static String construir(Object... pares) {
        UriComponentsBuilder constructor = UriComponentsBuilder.newInstance();
        for (int i = 0; i + 1 < pares.length; i += 2) {
            Object valor = pares[i + 1];
            if (valor != null && !valor.toString().isBlank()) {
                constructor.queryParam(pares[i].toString(), valor.toString());
            }
        }
        String consulta = constructor.build().encode().getQuery();
        return consulta == null ? "" : "&" + consulta;
    }
}
