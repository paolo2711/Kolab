package com.kolab.mensaje;

import com.kolab.perfil.Persona;
import java.util.List;

public record ConversacionDetalle(Long idOferta,
                                  String solicitud,
                                  Persona con,
                                  String enlace,
                                  String etiquetaEnlace,
                                  List<MensajeResumen> mensajes) {
}
