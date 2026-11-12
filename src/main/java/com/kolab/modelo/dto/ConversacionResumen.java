package com.kolab.modelo.dto;

import com.kolab.modelo.dto.Persona;

public record ConversacionResumen(String titulo,
                                  Persona con,
                                  String ultimo,
                                  String hace,
                                  boolean sinLeer,
                                  String enlace) {
}
