package com.backend.depahub.controllers;

import com.backend.depahub.models.Administrador;
import com.backend.depahub.services.AdministradorService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/administradores")
public class AdministradorController extends BaseCrudController<Administrador> {
    public AdministradorController(AdministradorService service) {
        super(service);
    }
}
