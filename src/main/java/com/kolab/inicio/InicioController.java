package com.kolab.inicio;

import com.kolab.demo.ArchivosDeDatos;
import com.kolab.demo.CatalogoDeCategorias;
import com.kolab.demo.DirectorioDePersonas;
import com.kolab.demo.SolicitudesDeEjemplo;
import com.kolab.categoria.CategoriaResumen;
import com.kolab.perfil.BienvenidaController;
import com.kolab.perfil.PerfilService;
import java.util.List;
import jakarta.servlet.http.HttpSession;
import com.kolab.solicitud.SolicitudResumen;
import com.kolab.usuario.UsuarioAutenticado;
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
    private static final int OPORTUNIDADES = 3;

    private final SolicitudesDeEjemplo solicitudes;
    private final CatalogoDeCategorias catalogo;
    private final DirectorioDePersonas directorio;
    private final ArchivosDeDatos archivos;
    private final PerfilService perfilService;

    public InicioController(SolicitudesDeEjemplo solicitudes, CatalogoDeCategorias catalogo,
                            DirectorioDePersonas directorio, ArchivosDeDatos archivos,
                            PerfilService perfilService) {
        this.solicitudes = solicitudes;
        this.catalogo = catalogo;
        this.directorio = directorio;
        this.archivos = archivos;
        this.perfilService = perfilService;
    }

    @GetMapping("/")
    public String inicio(@AuthenticationPrincipal UsuarioAutenticado usuario,
                         HttpSession sesion,
                         Model model) {
        if (usuario == null) {
            model.addAttribute("categorias", catalogo.categorias());
            model.addAttribute("solicitudes", solicitudes.catalogo().stream().limit(EN_PORTADA).toList());
            model.addAttribute("plataforma", archivos.plataforma());
            return "portada";
        }

        // a quien todavía no dijo qué sabe hacer se le pregunta una vez, no en cada visita
        if (!usuario.ofreceServicios() && sesion.getAttribute(BienvenidaController.YA_PREGUNTADO) == null) {
            return "redirect:/bienvenida";
        }

        List<Long> mias = perfilService.categoriasDe(usuario.getIdUsuario());
        List<CategoriaResumen> misCategorias = catalogo.categorias().stream()
                .filter(c -> mias.contains(c.id()))
                .toList();

        model.addAttribute("seccion", "inicio");
        model.addAttribute("usuario", usuario);
        model.addAttribute("categorias", catalogo.categorias());
        model.addAttribute("misCategorias", misCategorias);

        var perfil = archivos.perfilDe(usuario.esExperto());
        List<SolicitudResumen> cercanas = solicitudes.cercaDe(perfil.latitud(), perfil.longitud(), CERCANAS);
        model.addAttribute("miDistrito", perfil.distrito());
        model.addAttribute("cercanas", cercanas);
        model.addAttribute("expertos", directorio.mejoresExpertos(MEJORES_EXPERTOS));

        List<String> nombres = misCategorias.stream().map(CategoriaResumen::nombre).toList();
        List<SolicitudResumen> deLoMio = solicitudes.catalogo().stream()
                .filter(s -> nombres.contains(s.categoria()))
                .toList();
        List<SolicitudResumen> lista = deLoMio.isEmpty() ? solicitudes.catalogo() : deLoMio;

        // lo que ya salió en "cerca de ti" no se repite más abajo, salvo que al quitarlo
        // el bloque quede casi vacío: más vale repetir una que dejar un hueco
        List<Long> yaMostradas = cercanas.stream().map(SolicitudResumen::id).toList();
        List<SolicitudResumen> conDistancia = solicitudes.conDistancia(lista, perfil.latitud(), perfil.longitud());
        List<SolicitudResumen> sinRepetir = conDistancia.stream()
                .filter(s -> !yaMostradas.contains(s.id()))
                .toList();
        List<SolicitudResumen> restantes = sinRepetir.size() >= 2 ? sinRepetir : conDistancia;

        model.addAttribute("filtradas", !deLoMio.isEmpty());
        model.addAttribute("oportunidades", restantes.stream().limit(OPORTUNIDADES).toList());
        return "inicio/inicio-usuario";
    }

}
