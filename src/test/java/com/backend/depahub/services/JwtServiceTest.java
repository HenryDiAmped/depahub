package com.backend.depahub.services;

import com.backend.depahub.models.Administrador;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET = "clave-de-prueba-jwt-de-32-caracteres-minimo";

    @Test
    void generaYLeeUnTokenFirmado() {
        JwtService jwtService = new JwtService(SECRET, 3_600_000);
        Administrador administrador = new Administrador();
        administrador.setId(7L);
        administrador.setEmail("admin@depahub.test");

        String token = jwtService.generarToken(administrador);

        assertEquals("admin@depahub.test", jwtService.extraerEmail(token));
    }

    @Test
    void rechazaUnTokenAlterado() {
        JwtService jwtService = new JwtService(SECRET, 3_600_000);

        assertThrows(JwtException.class, () -> jwtService.extraerEmail("token.invalido.firmado"));
    }

    @Test
    void rechazaUnSecretDeMenosDe32Caracteres() {
        assertThrows(IllegalStateException.class, () -> new JwtService("secret-corto", 3_600_000));
    }

    @Test
    void rechazaUnaExpiracionMenorOIgualACero() {
        assertThrows(IllegalStateException.class, () -> new JwtService(SECRET, 0));
        assertThrows(IllegalStateException.class, () -> new JwtService(SECRET, -1));
    }

    @Test
    void rechazaUnTokenExpirado() throws InterruptedException {
        JwtService jwtService = new JwtService(SECRET, 1);
        Administrador administrador = new Administrador();
        administrador.setId(7L);
        administrador.setEmail("admin@depahub.test");

        String token = jwtService.generarToken(administrador);
        Thread.sleep(50);

        assertThrows(JwtException.class, () -> jwtService.extraerEmail(token));
    }
}
