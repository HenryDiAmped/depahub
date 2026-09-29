package com.backend.depahub.controllers;

import com.backend.depahub.models.Contrato;
import com.backend.depahub.services.ContratoService;
import com.backend.depahub.services.ContratoPdfService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contratos")
public class ContratoController extends BaseCrudController<Contrato> {

    private final ContratoService service;
    private final ContratoPdfService contratoPdfService;

    public ContratoController(ContratoService service, ContratoPdfService contratoPdfService) {
        super(service);
        this.service = service;
        this.contratoPdfService = contratoPdfService;
    }

    @GetMapping(params = "administradorId")
    public List<Contrato> listarPorAdministrador(@RequestParam Long administradorId) {
        return service.listarPorAdministrador(administradorId);
    }

    @GetMapping(params = "inquilinoId")
    public List<Contrato> listarPorInquilino(@RequestParam Long inquilinoId) {
        return service.listarPorInquilino(inquilinoId);
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<ByteArrayResource> descargarPdf(@PathVariable Long id, Authentication authentication) {
        Contrato contrato = service.obtenerParaAdministrador(id, authentication.getName());
        byte[] pdf = contratoPdfService.generar(contrato);
        String archivo = "contrato-" + contrato.getId() + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(archivo).build().toString())
                .body(new ByteArrayResource(pdf));
    }
}
