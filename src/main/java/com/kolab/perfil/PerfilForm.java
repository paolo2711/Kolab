package com.kolab.perfil;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

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

    // las dos opcionales; si no se sube otra se queda la que habia
    private MultipartFile foto;
    private MultipartFile portada;
    private boolean quitarFoto;
    private boolean quitarPortada;

    // solo para la vista previa, guardar no las lee
    private String fotoActual;
    private String portadaActual;

    public MultipartFile getFoto() {
        return foto;
    }

    public void setFoto(MultipartFile foto) {
        this.foto = foto;
    }

    public MultipartFile getPortada() {
        return portada;
    }

    public void setPortada(MultipartFile portada) {
        this.portada = portada;
    }

    public boolean isQuitarFoto() {
        return quitarFoto;
    }

    public void setQuitarFoto(boolean quitarFoto) {
        this.quitarFoto = quitarFoto;
    }

    public boolean isQuitarPortada() {
        return quitarPortada;
    }

    public void setQuitarPortada(boolean quitarPortada) {
        this.quitarPortada = quitarPortada;
    }

    public String getFotoActual() {
        return fotoActual;
    }

    public void setFotoActual(String fotoActual) {
        this.fotoActual = fotoActual;
    }

    public String getPortadaActual() {
        return portadaActual;
    }

    public void setPortadaActual(String portadaActual) {
        this.portadaActual = portadaActual;
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
