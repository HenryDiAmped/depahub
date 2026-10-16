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
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContratoServiceTest {

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private BalanceMensualService balanceMensualService;

    private ContratoService service;

    @BeforeEach
    void setUp() {
        service = new ContratoService(contratoRepository, cuentaRepository, balanceMensualService);
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
    void rechazaUnContratoDeMenosDeUnMes() {
        Contrato contrato = contrato(LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 31), 1);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.prepararCreacion(contrato));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void rechazaUnaFrecuenciaMayorQueElRangoDelContrato() {
        Contrato contrato = contrato(LocalDate.of(2026, 10, 2), LocalDate.of(2026, 11, 1), 2);

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
        assertEquals(LocalDate.of(2026, 11, 2), cuentas.get(1).getFechaVencimiento());
        assertEquals(LocalDate.of(2026, 12, 2), cuentas.get(2).getFechaVencimiento());
        assertEquals(LocalDate.of(2026, 9, 29), cuentas.get(1).getFechaEmitida());
    }

    @Test
    void creaElBalanceDelMesActualCuandoElContratoEstaVigente() {
        LocalDate hoy = LocalDate.now();
        Contrato contrato = contrato(hoy.minusDays(1), hoy.plusMonths(1), 1);
        contrato.setId(11L);
        contrato.setGarantia(new BigDecimal("500.00"));
        Administrador administrador = new Administrador();
        administrador.setId(3L);
        contrato.setAdministrador(administrador);
        contrato.setInquilino(new Inquilino());
        when(contratoRepository.save(any(Contrato.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.crear(contrato);

        verify(balanceMensualService).obtenerOCrear(administrador, YearMonth.from(hoy));
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
        assertEquals(LocalDate.of(2026, 5, 15), prorrateada.getFechaVencimiento());
        assertEquals(new BigDecimal("245.90"), prorrateada.getImporte());
        assertEquals("Cuota prorrateada 3 de 3 - Contrato #10", prorrateada.getConcepto());
    }

    @Test
    void listaLosContratosPorAdministrador() {
        Contrato contrato = contrato(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 1);
        when(contratoRepository.findByAdministradorId(3L)).thenReturn(List.of(contrato));

        assertEquals(List.of(contrato), service.listarPorAdministrador(3L));
        verify(contratoRepository).findByAdministradorId(3L);
    }

    @Test
    void listaLosContratosPorInquilino() {
        Contrato contrato = contrato(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 1);
        when(contratoRepository.findByInquilinoId(8L)).thenReturn(List.of(contrato));

        assertEquals(List.of(contrato), service.listarPorInquilino(8L));
        verify(contratoRepository).findByInquilinoId(8L);
    }

    @Test
    void entregaElContratoSiElAdministradorEsElDueno() {
        Contrato contrato = contratoConAdministrador("ana@depahub.test");
        when(contratoRepository.findById(9L)).thenReturn(Optional.of(contrato));

        assertEquals(contrato, service.obtenerParaAdministrador(9L, "ana@depahub.test"));
    }

    @Test
    void rechazaElAccesoSiElAdministradorNoEsElDueno() {
        Contrato contrato = contratoConAdministrador("ana@depahub.test");
        when(contratoRepository.findById(9L)).thenReturn(Optional.of(contrato));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.obtenerParaAdministrador(9L, "otro@depahub.test"));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void rechazaFechasDeInicioOFinNulas() {
        Contrato sinInicio = contrato(null, LocalDate.of(2026, 12, 31), 1);
        Contrato sinFin = contrato(LocalDate.of(2026, 1, 1), null, 1);

        ResponseStatusException sinInicioException = assertThrows(
                ResponseStatusException.class,
                () -> service.prepararCreacion(sinInicio));
        ResponseStatusException sinFinException = assertThrows(
                ResponseStatusException.class,
                () -> service.prepararCreacion(sinFin));

        assertEquals(HttpStatus.BAD_REQUEST, sinInicioException.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, sinFinException.getStatusCode());
    }

    @Test
    void noCreaElBalanceDelMesActualCuandoElContratoNoEstaVigente() {
        LocalDate hoy = LocalDate.now();
        Contrato contrato = contrato(hoy.plusDays(2), hoy.plusMonths(2), 1);
        contrato.setId(12L);
        contrato.setGarantia(new BigDecimal("500.00"));
        contrato.setAdministrador(new Administrador());
        contrato.setInquilino(new Inquilino());
        when(contratoRepository.save(any(Contrato.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.crear(contrato);

        verify(balanceMensualService, never()).obtenerOCrear(any(), any());
    }

    private Contrato contratoConAdministrador(String email) {
        Administrador administrador = new Administrador();
        administrador.setEmail(email);
        Contrato contrato = contrato(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 1);
        contrato.setId(9L);
        contrato.setAdministrador(administrador);
        return contrato;
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
