package com.kolab.servicio.impl;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

// brevo se reemplaza por un servidor falso: la prueba no sale a internet ni gasta envios
@Tag("unitaria")
class CorreoServiceImplTest {

    private static final String BREVO = "https://api.brevo.com/v3/smtp/email";
    private static final String ENLACE = "https://kolab.pe/recuperar/abc.def";

    private RestClient.Builder builder;
    private MockRestServiceServer brevo;

    @BeforeEach
    void preparar() {
        builder = RestClient.builder();
        brevo = MockRestServiceServer.bindTo(builder).build();
    }

    @Test
    void mandaElEnlaceAlCorreoConLaClaveDeBrevo() {
        brevo.expect(requestTo(BREVO))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("api-key", "clave-de-prueba"))
                .andExpect(jsonPath("$.to[0].email").value("paolo@kolab.pe"))
                .andExpect(jsonPath("$.sender.email").value("kolab@correo.pe"))
                .andExpect(content().string(containsString(ENLACE)))
                .andRespond(withSuccess("{\"messageId\":\"1\"}", MediaType.APPLICATION_JSON));

        servicio("clave-de-prueba").enviarEnlaceDeRecuperacion("paolo@kolab.pe", ENLACE);

        brevo.verify();
    }

    @Test
    void sinClaveNoLlamaABrevo() {
        servicio("").enviarEnlaceDeRecuperacion("paolo@kolab.pe", ENLACE);

        brevo.verify();
    }

    @Test
    void siBrevoRechazaLaClaveLaPantallaSigueIgual() {
        brevo.expect(requestTo(BREVO))
                .andRespond(withUnauthorizedRequest());

        assertThatCode(() -> servicio("clave-vencida").enviarEnlaceDeRecuperacion("paolo@kolab.pe", ENLACE))
                .doesNotThrowAnyException();
        brevo.verify();
    }

    private CorreoServiceImpl servicio(String clave) {
        return new CorreoServiceImpl(builder, BREVO, clave, "kolab@correo.pe");
    }
}
