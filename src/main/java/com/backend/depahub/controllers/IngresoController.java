package com.backend.depahub.controllers;

import com.backend.depahub.models.Ingreso;
import com.backend.depahub.services.IngresoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ingresos")
public class IngresoController extends BaseCrudController<Ingreso> {

    private final IngresoService service;

    public IngresoController(IngresoService service) {
        super(service);
        this.service = service;
    }

    @GetMapping(params = "balanceId")
    public List<Ingreso> listarPorBalance(@RequestParam Long balanceId) {
        return service.listarPorBalance(balanceId);
    }
}
