package com.backend.depahub.controllers;

import com.backend.depahub.models.Propiedad;
import com.backend.depahub.services.PropiedadService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/propiedades")
public class PropiedadController extends BaseCrudController<Propiedad> {

    private final PropiedadService service;

    public PropiedadController(PropiedadService service) {
        super(service);
        this.service = service;
    }

    @GetMapping(params = "administradorId")
    public List<Propiedad> listarPorAdministrador(@RequestParam Long administradorId) {
        return service.listarPorAdministrador(administradorId);
    }
}
