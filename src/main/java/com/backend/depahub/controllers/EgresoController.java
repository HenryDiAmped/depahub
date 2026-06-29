package com.backend.depahub.controllers;

import com.backend.depahub.models.Egreso;
import com.backend.depahub.services.EgresoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/egresos")
public class EgresoController extends BaseCrudController<Egreso> {

    private final EgresoService service;

    public EgresoController(EgresoService service) {
        super(service);
        this.service = service;
    }

    @GetMapping(params = "balanceId")
    public List<Egreso> listarPorBalance(@RequestParam Long balanceId) {
        return service.listarPorBalance(balanceId);
    }
}
