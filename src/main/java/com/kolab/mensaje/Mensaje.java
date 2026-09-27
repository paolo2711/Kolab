package com.kolab.mensaje;

import com.kolab.oferta.Oferta;
import com.kolab.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Un mensaje de la conversación. Cuelga de la oferta, no del usuario: en KOLAB no existe el chat
 * suelto, la conversación nace con la oferta y vive dentro de ella.
 */
@Entity
@Table(name = "mensaje")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_oferta", nullable = false)
    private Oferta oferta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_emisor", nullable = false)
    private Usuario emisor;

    @Column(nullable = false, length = 1000)
    private String contenido;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio = LocalDateTime.now();

    @Column(nullable = false)
    private boolean leido = false;

    protected Mensaje() {
    }

    public Mensaje(Oferta oferta, Usuario emisor, String contenido) {
        this.oferta = oferta;
        this.emisor = emisor;
        this.contenido = contenido;
    }

    /**
     * Marca el mensaje como visto por quien lo recibió. Es lo que apaga el contador del menú.
     */
    public void marcarLeido() {
        this.leido = true;
    }

    public Long getId() {
        return id;
    }

    public Oferta getOferta() {
        return oferta;
    }

    public Usuario getEmisor() {
        return emisor;
    }

    public String getContenido() {
        return contenido;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public boolean isLeido() {
        return leido;
    }
}
