package com.kolab.inicio;

import com.kolab.categoria.CategoriaService;
import com.kolab.common.Fotos;
import com.kolab.perfil.BienvenidaController;
import com.kolab.perfil.DirectorioService;
import com.kolab.reporte.ReporteService;
import com.kolab.solicitud.CatalogoService;
import com.kolab.solicitud.SolicitudResumen;
import com.kolab.usuario.UsuarioAutenticado;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioController {

    // cuántas piezas entra cada bloque de la pantalla, que es decisión de maqueta y no dato
    private static final int EN_PORTADA = 4;
    private static final int CERCANAS = 6;
    private static final int MEJORES_EXPERTOS = 4;
    private static final int OPORTUNIDADES = 5;

    private final CatalogoService catalogoService;
    private final CategoriaService categoriaService;
    private final DirectorioService directorioService;
    private final ReporteService reporteService;
    private final Fotos fotos;

    public InicioController(CatalogoService catalogoService, CategoriaService categoriaService,
                            DirectorioService directorioService, ReporteService reporteService,
                            Fotos fotos) {
        this.catalogoService = catalogoService;
        this.categoriaService = categoriaService;
        this.directorioService = directorioService;
        this.reporteService = reporteService;
        this.fotos = fotos;
    }

    @GetMapping("/")
    public String inicio(@AuthenticationPrincipal UsuarioAutenticado usuario, HttpSession sesion, Model model) {
        if (usuario == null) {
            model.addAttribute("categorias", categoriaService.activas());
            model.addAttribute("solicitudes", catalogoService.recientesPublicas(EN_PORTADA));
            model.addAttribute("plataforma", reporteService.plataforma());
            model.addAttribute("fotoAcceso", fotos.suelta("acceso"));
            return "portada";
        }

        // a quien todavía no dijo qué sabe hacer se le pregunta una vez, no en cada visita
        if (!usuario.ofreceServicios() && sesion.getAttribute(BienvenidaController.YA_PREGUNTADO) == null) {
            return "redirect:/bienvenida";
        }

        Long yo = usuario.getIdUsuario();
        List<SolicitudResumen> cercanas = catalogoService.cercanas(yo, CERCANAS);
        List<SolicitudResumen> deLoMio = catalogoService.enMisCategorias(yo, OPORTUNIDADES + CERCANAS);
        List<SolicitudResumen> lista = deLoMio.isEmpty() ? catalogoService.recientes(yo, OPORTUNIDADES + CERCANAS) : deLoMio;

        model.addAttribute("seccion", "inicio");
        model.addAttribute("usuario", usuario);
        model.addAttribute("categorias", categoriaService.activas());
        model.addAttribute("cercanas", cercanas);
        model.addAttribute("expertos", directorioService.mejorCalificados(MEJORES_EXPERTOS));
        model.addAttribute("filtradas", !deLoMio.isEmpty());
        model.addAttribute("oportunidades", sinRepetir(lista, cercanas));
        return "inicio/inicio-usuario";
    }

    // lo que ya salió en "cerca de ti" no se repite más abajo, salvo que al quitarlo el bloque
    // quede casi vacío: más vale repetir una que dejar un hueco
    private List<SolicitudResumen> sinRepetir(List<SolicitudResumen> lista, List<SolicitudResumen> cercanas) {
        List<Long> yaMostradas = cercanas.stream().map(SolicitudResumen::id).toList();
        List<SolicitudResumen> restantes = lista.stream().filter(s -> !yaMostradas.contains(s.id())).toList();
        return (restantes.size() >= 2 ? restantes : lista).stream().limit(OPORTUNIDADES).toList();
    }
}
