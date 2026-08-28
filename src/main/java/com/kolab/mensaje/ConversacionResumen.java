package com.kolab.mensaje;

import com.kolab.perfil.Persona;

public record ConversacionResumen(String titulo,
                                  Persona con,
                                  String ultimo,
                                  String hace,
                                  boolean sinLeer,
                                  String enlace) {
}
