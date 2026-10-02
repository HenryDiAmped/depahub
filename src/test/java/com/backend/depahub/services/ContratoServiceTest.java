package com.backend.depahub.services;

import com.backend.depahub.models.Contrato;
import com.backend.depahub.repositorys.ContratoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ContratoServiceTest {

    private ContratoService service;

    @BeforeEach
    void setUp() {
        service = new ContratoService(mock(ContratoRepository.class));
    }

    @Test
    void calculaDoceCuotasMensualesParaUnAnioCompleto() {
        Contrato contrato = contrato(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 1);

        service.prepararCreacion(contrato);

        assertEquals(12, contrato.getNumeroCuotas());
    }

    @Test
    void calculaCuotasBimestralesParaPeriodosCompletos() {
        Contrato contrato = contrato(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 2);

        service.prepararCreacion(contrato);

        assertEquals(3, contrato.getNumeroCuotas());
    }

    @Test
    void agregaUnaCuotaPorElPeriodoProrrateadoFinal() {
        Contrato contrato = contrato(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 5, 15), 2);

        service.prepararCreacion(contrato);

        assertEquals(3, contrato.getNumeroCuotas());
    }

    @Test
    void admiteUnaFrecuenciaPersonalizada() {
        Contrato contrato = contrato(LocalDate.of(2026, 1, 10), LocalDate.of(2026, 10, 9), 3);

        service.prepararCreacion(contrato);

        assertEquals(3, contrato.getNumeroCuotas());
    }

    @Test
    void rechazaUnaFrecuenciaNoPositiva() {
        Contrato contrato = contrato(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), 0);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.prepararCreacion(contrato));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void rechazaUnaFechaFinalAnteriorALaInicial() {
        Contrato contrato = contrato(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 31), 1);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.prepararCreacion(contrato));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    private Contrato contrato(LocalDate inicio, LocalDate fin, int frecuencia) {
        Contrato contrato = new Contrato();
        contrato.setFechaInicio(inicio);
        contrato.setFechaFin(fin);
        contrato.setFrecuencia(frecuencia);
        return contrato;
    }
}
