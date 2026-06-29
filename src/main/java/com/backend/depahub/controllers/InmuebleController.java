package com.backend.depahub.controllers;

import com.backend.depahub.models.Inmueble;
import com.backend.depahub.services.InmuebleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inmuebles")
public class InmuebleController extends BaseCrudController<Inmueble> {

    private final InmuebleService service;

    public InmuebleController(InmuebleService service) {
        super(service);
        this.service = service;
    }

    @GetMapping(params = "propiedadId")
    public List<Inmueble> listarPorPropiedad(@RequestParam Long propiedadId) {
        return service.listarPorPropiedad(propiedadId);
    }
}
