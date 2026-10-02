package com.backend.depahub.services;

import com.backend.depahub.models.Contrato;
import com.backend.depahub.models.Cuenta;
import com.backend.depahub.models.Administrador;
import com.backend.depahub.models.Inquilino;
import com.backend.depahub.repositorys.ContratoRepository;
import com.backend.depahub.repositorys.CuentaRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContratoServiceTest {

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    private ContratoService service;

    @BeforeEach
    void setUp() {
        service = new ContratoService(contratoRepository, cuentaRepository);
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

    @Test
    void creaGarantiaYCuotasConSusVencimientos() {
        Contrato contrato = contrato(LocalDate.of(2026, 10, 2), LocalDate.of(2026, 12, 1), 1);
        contrato.setId(9L);
        contrato.setFechaRegistro(LocalDate.of(2026, 9, 29));
        contrato.setGarantia(new BigDecimal("500.00"));
        contrato.setAdministrador(new Administrador());
        contrato.setInquilino(new Inquilino());
        when(contratoRepository.save(any(Contrato.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.crear(contrato);

        ArgumentCaptor<Iterable<Cuenta>> cuentasCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(cuentaRepository).saveAll(cuentasCaptor.capture());
        List<Cuenta> cuentas = (List<Cuenta>) cuentasCaptor.getValue();
        assertEquals(3, cuentas.size());
        assertEquals(LocalDate.of(2026, 10, 5), cuentas.get(0).getFechaVencimiento());
        assertEquals(LocalDate.of(2026, 10, 2), cuentas.get(1).getFechaVencimiento());
        assertEquals(LocalDate.of(2026, 11, 2), cuentas.get(2).getFechaVencimiento());
        assertEquals(LocalDate.of(2026, 9, 29), cuentas.get(1).getFechaEmitida());
    }

    @Test
    void creaUnaCuotaProrrateadaConImporteProporcional() {
        Contrato contrato = contrato(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 5, 15), 2);
        contrato.setId(10L);
        contrato.setFechaRegistro(LocalDate.of(2025, 12, 30));
        contrato.setGarantia(new BigDecimal("500.00"));
        contrato.setAdministrador(new Administrador());
        contrato.setInquilino(new Inquilino());
        when(contratoRepository.save(any(Contrato.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.crear(contrato);

        ArgumentCaptor<Iterable<Cuenta>> cuentasCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(cuentaRepository).saveAll(cuentasCaptor.capture());
        List<Cuenta> cuentas = (List<Cuenta>) cuentasCaptor.getValue();
        Cuenta prorrateada = cuentas.get(3);
        assertEquals(LocalDate.of(2026, 5, 1), prorrateada.getFechaVencimiento());
        assertEquals(new BigDecimal("245.90"), prorrateada.getImporte());
        assertEquals("Cuota prorrateada 3 de 3 - Contrato #10", prorrateada.getConcepto());
    }

    private Contrato contrato(LocalDate inicio, LocalDate fin, int frecuencia) {
        Contrato contrato = new Contrato();
        contrato.setFechaInicio(inicio);
        contrato.setFechaFin(fin);
        contrato.setFrecuencia(frecuencia);
        contrato.setMontoAlquiler(new BigDecimal("1000.00"));
        return contrato;
    }
}
