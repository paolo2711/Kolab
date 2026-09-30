package com.kolab.panel;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PanelController {

    private final PanelService panelService;

    public PanelController(PanelService panelService) {
        this.panelService = panelService;
    }

    @GetMapping("/panel")
    public String panel(Model model) {
        model.addAttribute("seccion", "panel");
        model.addAttribute("resumen", panelService.resumen());
        return "panel/panel";
    }
}
