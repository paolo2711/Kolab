package com.kolab.servicio.impl;

import com.kolab.servicio.CorreoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Implementacion de {@link CorreoService} con la API de Brevo.
 */
@Service
public class CorreoServiceImpl implements CorreoService {

    private final RestClient brevo;
    private final String clave;
    private final String remitente;

    public CorreoServiceImpl(RestClient.Builder builder,
                             @Value("${kolab.correo.clave-brevo:}") String clave,
                             @Value("${kolab.correo.remitente:}") String remitente) {
        this.brevo = builder.build();
        this.clave = clave;
        this.remitente = remitente;
    }

    @Override
    public void enviarEnlaceDeRecuperacion(String email, String enlace) {
    }
}
