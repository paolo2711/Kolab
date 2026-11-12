package com.kolab.modelo.dto;

/**
 * Una cuenta como la ve quien administra: nombre, correo, cuándo se registró y si está activa.
 */
public record CuentaResumen(Long id, String nombre, String email, String registrada, boolean activa) {
}
