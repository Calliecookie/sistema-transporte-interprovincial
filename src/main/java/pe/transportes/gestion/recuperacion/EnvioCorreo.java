package pe.transportes.gestion.recuperacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EnvioCorreo {

    private static final Logger REGISTRO = LoggerFactory.getLogger(EnvioCorreo.class);

    private final ObjectProvider<JavaMailSender> emisor;
    private final PropiedadesAplicacion propiedades;
    private final String servidor;

    public EnvioCorreo(ObjectProvider<JavaMailSender> emisor, PropiedadesAplicacion propiedades,
            @Value("${spring.mail.host:}") String servidor) {
        this.emisor = emisor;
        this.propiedades = propiedades;
        this.servidor = servidor;
    }

    public void enviar(String destino, String asunto, String texto) {
        JavaMailSender sender = emisor.getIfAvailable();
        if (sender == null || servidor == null || servidor.isBlank()) {
            REGISTRO.warn("El correo no está configurado. Mensaje para {}: {}", destino, texto);
            return;
        }
        SimpleMailMessage mensaje = new SimpleMailMessage();
        if (propiedades.correoRemitente() != null && !propiedades.correoRemitente().isBlank()) {
            mensaje.setFrom(propiedades.correoRemitente());
        }
        mensaje.setTo(destino);
        mensaje.setSubject(asunto);
        mensaje.setText(texto);
        try {
            sender.send(mensaje);
        } catch (MailException excepcion) {
            REGISTRO.error("No se pudo enviar el correo a {}: {}", destino, excepcion.getMessage());
        }
    }
}
