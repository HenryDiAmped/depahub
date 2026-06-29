package com.backend.depahub.controllers;

import com.backend.depahub.models.Inquilino;
import com.backend.depahub.services.InquilinoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inquilinos")
public class InquilinoController extends BaseCrudController<Inquilino> {

    private final InquilinoService service;

    public InquilinoController(InquilinoService service) {
        super(service);
        this.service = service;
    }

    @GetMapping(params = "inmuebleId")
    public List<Inquilino> listarPorInmueble(@RequestParam Long inmuebleId) {
        return service.listarPorInmueble(inmuebleId);
    }
}
