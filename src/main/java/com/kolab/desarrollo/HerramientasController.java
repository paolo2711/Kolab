package com.kolab.desarrollo;

import com.kolab.common.RecursoNoEncontradoException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// solo existe con kolab.desarrollo=true: en el servidor estas rutas no están y responden 404
@Controller
@RequestMapping("/desarrollo")
@ConditionalOnProperty(name = "kolab.desarrollo", havingValue = "true")
class HerramientasController {

    private final ReinicioDeBase reinicio;
    private final CargaDeEjemplos carga;

    HerramientasController(ReinicioDeBase reinicio, CargaDeEjemplos carga) {
        this.reinicio = reinicio;
        this.carga = carga;
    }

    @PostMapping("/reiniciar")
    public String reiniciar(HttpServletRequest pedido, RedirectAttributes flash) throws ServletException {
        soloDesdeEstaMaquina(pedido);
        reinicio.reiniciar();
        // la cuenta con la que se estaba dentro puede no existir ya
        pedido.logout();
        flash.addFlashAttribute("aviso", "Base reiniciada con los ejemplos.");
        return "redirect:/login";
    }

    @PostMapping("/ejemplos")
    public String cargarEjemplos(HttpServletRequest pedido, RedirectAttributes flash) {
        soloDesdeEstaMaquina(pedido);
        flash.addFlashAttribute("aviso", carga.cargarSiVacia()
                ? "Ejemplos cargados."
                : "La base ya tiene datos. Para empezar de cero, reinicia los datos.");
        return "redirect:/login";
    }

    /**
     * Corta el pedido si no viene de la misma máquina. Estas rutas borran y rellenan la base, así
     * que la variable de entorno no alcanza como única llave: basta que alguien la deje prendida en
     * un servidor para dejar la base a merced de cualquiera que sepa la dirección.
     */
    private void soloDesdeEstaMaquina(HttpServletRequest pedido) {
        if (!esDeEstaMaquina(pedido.getRemoteAddr())) {
            throw new RecursoNoEncontradoException("Las herramientas de desarrollo solo responden desde la propia máquina");
        }
    }

    private boolean esDeEstaMaquina(String direccion) {
        try {
            return InetAddress.getByName(direccion).isLoopbackAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
