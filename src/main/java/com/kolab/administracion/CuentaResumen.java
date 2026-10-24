package com.kolab.administracion;

/**
 * Una cuenta como la ve quien administra: nombre, correo, cuándo se registró y si está activa.
 */
public record CuentaResumen(String nombre, String email, String registrada, boolean activa) {
}
