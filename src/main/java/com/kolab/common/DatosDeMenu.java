package com.kolab.common;

import com.kolab.mensaje.MensajeService;
import com.kolab.oferta.OfertaService;
import com.kolab.solicitud.SolicitudService;
import com.kolab.usuario.UsuarioAutenticado;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// los contadores del menú y de las pestañas de Mi actividad los necesitan todas las pantallas
@ControllerAdvice
public class DatosDeMenu {

    private final MensajeService mensajeService;
    private final SolicitudService solicitudService;
    private final OfertaService ofertaService;

    public DatosDeMenu(MensajeService mensajeService, SolicitudService solicitudService,
                       OfertaService ofertaService) {
        this.mensajeService = mensajeService;
        this.solicitudService = solicitudService;
        this.ofertaService = ofertaService;
    }

    @ModelAttribute("mensajesSinLeer")
    public long mensajesSinLeer(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return usuario == null ? 0 : mensajeService.sinLeer(usuario.getIdUsuario());
    }

    @ModelAttribute("cuantasPedidas")
    public long cuantasPedidas(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return usuario == null ? 0 : solicitudService.cuantasMias(usuario.getIdUsuario());
    }

    @ModelAttribute("cuantasOfrecidas")
    public long cuantasOfrecidas(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return usuario == null ? 0 : ofertaService.cuantasMias(usuario.getIdUsuario());
    }
}
