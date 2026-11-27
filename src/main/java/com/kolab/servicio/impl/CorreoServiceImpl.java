package com.kolab.servicio.impl;

import com.kolab.servicio.CorreoService;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * {@link CorreoService} con la api de brevo. sin clave no manda nada, en desarrollo el enlace sale en pantalla
 */
@Service
public class CorreoServiceImpl implements CorreoService {

    private static final Logger log = LoggerFactory.getLogger(CorreoServiceImpl.class);

    private final RestClient brevo;
    private final String url;
    private final String clave;
    private final String remitente;

    public CorreoServiceImpl(RestClient.Builder builder,
                             @Value("${kolab.correo.url-brevo}") String url,
                             @Value("${kolab.correo.clave-brevo:}") String clave,
                             @Value("${kolab.correo.remitente:}") String remitente) {
        this.brevo = builder.build();
        this.url = url;
        this.clave = clave;
        this.remitente = remitente;
    }

    @Override
    public void enviarEnlaceDeRecuperacion(String email, String enlace) {
        if (clave.isBlank() || remitente.isBlank()) {
            log.warn("correo sin configurar (KOLAB_BREVO_CLAVE y KOLAB_CORREO_REMITENTE): el enlace no se envio");
            return;
        }
        try {
            brevo.post().uri(url)
                    .header("api-key", clave)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(mensaje(email, enlace))
                    .retrieve()
                    .toBodilessEntity();
            log.info("correo de recuperacion enviado");
        } catch (RestClientException e) {
            // la persona ve la misma pantalla y puede pedir otro enlace; el error queda en el historial
            log.error("brevo no envio el correo de recuperacion: {}", e.getMessage());
        }
    }

    private Map<String, Object> mensaje(String email, String enlace) {
        return Map.of(
                "sender", Map.of("name", "KOLAB", "email", remitente),
                "to", List.of(Map.of("email", email)),
                "subject", "Tu contraseña nueva de KOLAB",
                "htmlContent", "<p>Pediste poner una contraseña nueva en KOLAB.</p>"
                        + "<p><a href=\"" + enlace + "\">Poner contraseña nueva</a></p>"
                        + "<p>El enlace vence en 30 minutos. Si no lo pediste, no hagas nada: tu contraseña sigue igual.</p>");
    }
}
