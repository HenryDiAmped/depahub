package com.backend.depahub.controllers;

import com.backend.depahub.models.Contrato;
import com.backend.depahub.services.ContratoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contratos")
public class ContratoController extends BaseCrudController<Contrato> {

    private final ContratoService service;

    public ContratoController(ContratoService service) {
        super(service);
        this.service = service;
    }

    @GetMapping(params = "administradorId")
    public List<Contrato> listarPorAdministrador(@RequestParam Long administradorId) {
        return service.listarPorAdministrador(administradorId);
    }

    @GetMapping(params = "inquilinoId")
    public List<Contrato> listarPorInquilino(@RequestParam Long inquilinoId) {
        return service.listarPorInquilino(inquilinoId);
    }
}
