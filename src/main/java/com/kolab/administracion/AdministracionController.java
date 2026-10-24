package com.kolab.administracion;

import com.kolab.common.BitacoraDeSeguridad;
import com.kolab.reporte.DescargaExcel;
import com.kolab.usuario.UsuarioAutenticado;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// salvo el ingreso, solo llega quien tiene el rol: la cadena del panel en SecurityConfig cierra lo demás
@Controller
@RequestMapping("/admin")
public class AdministracionController {

    private static final int POR_PAGINA = 25;

    private final AdministracionService administracionService;
    private final TableroEnExcel enExcel;
    private final BitacoraDeSeguridad bitacora;

    public AdministracionController(AdministracionService administracionService, TableroEnExcel enExcel,
                                    BitacoraDeSeguridad bitacora) {
        this.administracionService = administracionService;
        this.enExcel = enExcel;
        this.bitacora = bitacora;
    }

    // el ingreso del panel usa la misma pantalla que el del mercado; la plantilla cambia lo que hace falta
    @GetMapping("/login")
    public String ingreso() {
        return "cuenta/login";
    }

    @GetMapping
    public String panel(Model model) {
        model.addAttribute("seccion", "panel");
        model.addAttribute("tablero", administracionService.tablero());
        return "administracion/panel";
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> excel() {
        return DescargaExcel.de("panel-kolab.xlsx", enExcel.de(administracionService.tablero()));
    }

    @GetMapping("/cuentas")
    public String cuentas(@RequestParam(defaultValue = "0") int pagina, Model model) {
        model.addAttribute("seccion", "cuentas");
        model.addAttribute("cuentas", administracionService.cuentas(PageRequest.of(Math.max(pagina, 0), POR_PAGINA)));
        return "administracion/cuentas";
    }

    @PostMapping("/cuentas/{id}/suspender")
    public String suspender(@PathVariable Long id, @RequestParam(defaultValue = "0") int pagina,
                            @AuthenticationPrincipal UsuarioAutenticado quien, HttpServletRequest pedido,
                            RedirectAttributes flash) {
        String email = administracionService.suspender(id, quien.getIdUsuario());
        bitacora.cuentaSuspendida(email, quien.getUsername(), pedido.getRemoteAddr());
        flash.addFlashAttribute("aviso", "Suspendiste la cuenta " + email + ". Ya no puede ingresar.");
        return volverALista(pagina);
    }

    @PostMapping("/cuentas/{id}/reactivar")
    public String reactivar(@PathVariable Long id, @RequestParam(defaultValue = "0") int pagina,
                            @AuthenticationPrincipal UsuarioAutenticado quien, HttpServletRequest pedido,
                            RedirectAttributes flash) {
        String email = administracionService.reactivar(id);
        bitacora.cuentaReactivada(email, quien.getUsername(), pedido.getRemoteAddr());
        flash.addFlashAttribute("aviso", "Reactivaste la cuenta " + email + ".");
        return volverALista(pagina);
    }

    // el aviso queda atado a la direccion exacta del regreso: la primera pagina va sin parametro
    private static String volverALista(int pagina) {
        return pagina > 0 ? "redirect:/admin/cuentas?pagina=" + pagina : "redirect:/admin/cuentas";
    }
}
