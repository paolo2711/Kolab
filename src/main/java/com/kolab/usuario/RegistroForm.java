package com.kolab.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistroForm {

    @NotBlank(message = "Escribe tu nombre")
    @Size(max = 80, message = "Máximo 80 caracteres")
    private String nombre;

    @NotBlank(message = "Escribe tus apellidos")
    @Size(max = 80, message = "Máximo 80 caracteres")
    private String apellidos;

    @NotBlank(message = "Escribe tu correo")
    @Email(message = "Ese correo no tiene un formato válido")
    @Size(max = 120, message = "Máximo 120 caracteres")
    private String email;

    @Pattern(regexp = "^$|^[0-9]{9}$", message = "El celular son 9 dígitos")
    private String telefono;

    @NotBlank(message = "Escribe una contraseña")
    @Size(min = 8, max = 64, message = "Entre 8 y 64 caracteres")
    private String password;

    @NotBlank(message = "Repite la contraseña")
    private String confirmacion;

    @NotNull(message = "Elige cómo vas a usar KOLAB")
    private TipoPerfil tipoPerfil = TipoPerfil.CLIENTE;

    public boolean contrasenasCoinciden() {
        return password != null && password.equals(confirmacion);
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmacion() {
        return confirmacion;
    }

    public void setConfirmacion(String confirmacion) {
        this.confirmacion = confirmacion;
    }

    public TipoPerfil getTipoPerfil() {
        return tipoPerfil;
    }

    public void setTipoPerfil(TipoPerfil tipoPerfil) {
        this.tipoPerfil = tipoPerfil;
    }
}
