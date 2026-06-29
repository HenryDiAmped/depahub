package com.backend.depahub.controllers;

import com.backend.depahub.models.Administrador;
import com.backend.depahub.services.AdministradorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AdministradorService administradorService;

    public AuthController(AdministradorService administradorService) {
        this.administradorService = administradorService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody Administrador administrador) {
        Administrador creado = administradorService.crear(administrador);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse("Registro exitoso", creado));
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        Administrador administrador = administradorService.login(request.email(), request.password());
        return new AuthResponse("Login exitoso", administrador);
    }

    public record LoginRequest(String email, String password) {
    }

    public record AuthResponse(String mensaje, Administrador administrador) {
    }
}
