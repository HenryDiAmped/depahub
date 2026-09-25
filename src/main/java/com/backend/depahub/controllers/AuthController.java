package com.backend.depahub.controllers;

import com.backend.depahub.models.Administrador;
import com.backend.depahub.services.AdministradorService;
import com.backend.depahub.services.JwtService;
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
    private final JwtService jwtService;

    public AuthController(AdministradorService administradorService, JwtService jwtService) {
        this.administradorService = administradorService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody Administrador administrador) {
        Administrador creado = administradorService.crear(administrador);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse("Registro exitoso", jwtService.generarToken(creado), creado));
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        Administrador administrador = administradorService.login(request.email(), request.password());
        return new AuthResponse("Login exitoso", jwtService.generarToken(administrador), administrador);
    }

    public record LoginRequest(String email, String password) {
    }

    public record AuthResponse(String mensaje, String token, Administrador administrador) {
    }
}
