package com.backend.depahub.controllers;

import com.backend.depahub.models.Cuenta;
import com.backend.depahub.services.CuentaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController extends BaseCrudController<Cuenta> {

    private final CuentaService service;

    public CuentaController(CuentaService service) {
        super(service);
        this.service = service;
    }

    @GetMapping(params = "administradorId")
    public List<Cuenta> listarPorAdministrador(@RequestParam Long administradorId) {
        return service.listarPorAdministrador(administradorId);
    }

    @GetMapping(params = "inquilinoId")
    public List<Cuenta> listarPorInquilino(@RequestParam Long inquilinoId) {
        return service.listarPorInquilino(inquilinoId);
    }
}
