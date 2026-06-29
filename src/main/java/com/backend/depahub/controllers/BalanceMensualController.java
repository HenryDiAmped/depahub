package com.backend.depahub.controllers;

import com.backend.depahub.models.BalanceMensual;
import com.backend.depahub.services.BalanceMensualService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/balances-mensuales")
public class BalanceMensualController extends BaseCrudController<BalanceMensual> {

    private final BalanceMensualService service;

    public BalanceMensualController(BalanceMensualService service) {
        super(service);
        this.service = service;
    }

    @GetMapping(params = "administradorId")
    public List<BalanceMensual> listarPorAdministrador(@RequestParam Long administradorId) {
        return service.listarPorAdministrador(administradorId);
    }
}
