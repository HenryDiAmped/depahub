package com.backend.depahub.services;

import com.backend.depahub.models.Cuenta;
import com.backend.depahub.repositorys.CuentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class CuentaServiceTest {

    private CuentaService service;

    @BeforeEach
    void setUp() {
        service = new CuentaService(mock(CuentaRepository.class));
    }

    @Test
    void permiteUnaCuentaManualSinFechaDeVencimiento() {
        Cuenta cuenta = cuentaConFechas(LocalDate.of(2026, 10, 1), null);

        assertDoesNotThrow(() -> service.prepararCreacion(cuenta));
    }

    @Test
    void rechazaUnVencimientoAnteriorALaEmision() {
        Cuenta cuenta = cuentaConFechas(LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 1));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.prepararCreacion(cuenta));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    private Cuenta cuentaConFechas(LocalDate emision, LocalDate vencimiento) {
        Cuenta cuenta = new Cuenta();
        cuenta.setFechaEmitida(emision);
        cuenta.setFechaVencimiento(vencimiento);
        return cuenta;
    }
}
