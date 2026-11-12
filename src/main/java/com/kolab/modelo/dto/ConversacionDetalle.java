package com.kolab.modelo.dto;

import java.util.List;

public record ConversacionDetalle(Long idOferta,
                                  String solicitud,
                                  Persona con,
                                  String enlace,
                                  String etiquetaEnlace,
                                  List<MensajeResumen> mensajes) {
}
