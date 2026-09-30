package com.kolab.perfil;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PerfilForm {

    @NotBlank(message = "Escribe tu nombre")
    @Size(max = 80, message = "Máximo 80 caracteres")
    private String nombre;

    @NotBlank(message = "Escribe tus apellidos")
    @Size(max = 80, message = "Máximo 80 caracteres")
    private String apellidos;

    @Pattern(regexp = "^$|^[0-9]{9}$", message = "El celular son 9 dígitos")
    private String telefono;

    @Size(max = 60, message = "Máximo 60 caracteres")
    private String distrito;

    @Size(max = 500, message = "Máximo 500 caracteres")
    private String descripcion;

    @Size(max = 500, message = "Máximo 500 caracteres")
    private String experiencia;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getExperiencia() {
        return experiencia;
    }

    public void setExperiencia(String experiencia) {
        this.experiencia = experiencia;
    }
}
