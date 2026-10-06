package com.backend.depahub.services;

import com.backend.depahub.models.Administrador;
import com.backend.depahub.models.BalanceMensual;
import com.backend.depahub.models.Cuenta;
import com.backend.depahub.repositorys.CuentaRepository;
import com.backend.depahub.repositorys.EgresoRepository;
import com.backend.depahub.repositorys.IngresoRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuentaServiceTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private BalanceMensualService balanceMensualService;

    @Mock
    private IngresoRepository ingresoRepository;

    @Mock
    private EgresoRepository egresoRepository;

    private CuentaService service;

    @BeforeEach
    void setUp() {
        service = new CuentaService(cuentaRepository, balanceMensualService, ingresoRepository, egresoRepository);
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

    @Test
    void alSaldarCuentasCreaMovimientosYActualizaLosTotalesDelBalanceActual() {
        Administrador administrador = new Administrador();
        administrador.setId(7L);
        BalanceMensual balance = new BalanceMensual();
        balance.setId(21L);
        balance.setTotalIngresos(new java.math.BigDecimal("100.00"));
        balance.setTotalEgresos(new java.math.BigDecimal("40.00"));

        Cuenta porCobrar = cuentaSaldada(1L, Cuenta.TipoCuenta.POR_COBRAR, new java.math.BigDecimal("250.00"), administrador);
        Cuenta porPagar = cuentaSaldada(2L, Cuenta.TipoCuenta.POR_PAGAR, new java.math.BigDecimal("60.00"), administrador);
        Cuenta cuentaPersistidaPorCobrar = cuentaPendiente(1L);
        Cuenta cuentaPersistidaPorPagar = cuentaPendiente(2L);
        when(cuentaRepository.findById(1L)).thenReturn(java.util.Optional.of(cuentaPersistidaPorCobrar));
        when(cuentaRepository.findById(2L)).thenReturn(java.util.Optional.of(cuentaPersistidaPorPagar));
        when(cuentaRepository.save(any(Cuenta.class))).thenAnswer(invocation -> {
            Cuenta cuentaGuardada = invocation.getArgument(0);
            // EntityManager.merge actualiza la instancia previamente cargada en la misma transacción.
            if (cuentaGuardada.getId().equals(1L)) {
                cuentaPersistidaPorCobrar.setEstado(cuentaGuardada.getEstado());
            } else {
                cuentaPersistidaPorPagar.setEstado(cuentaGuardada.getEstado());
            }
            return cuentaGuardada;
        });
        when(balanceMensualService.obtenerOCrear(any(Administrador.class), any(java.time.YearMonth.class))).thenReturn(balance);

        service.actualizar(1L, porCobrar);
        service.actualizar(2L, porPagar);

        ArgumentCaptor<com.backend.depahub.models.Ingreso> ingresoCaptor = ArgumentCaptor.forClass(com.backend.depahub.models.Ingreso.class);
        ArgumentCaptor<com.backend.depahub.models.Egreso> egresoCaptor = ArgumentCaptor.forClass(com.backend.depahub.models.Egreso.class);
        verify(ingresoRepository).save(ingresoCaptor.capture());
        verify(egresoRepository).save(egresoCaptor.capture());
        assertEquals(new java.math.BigDecimal("350.00"), balance.getTotalIngresos());
        assertEquals(new java.math.BigDecimal("100.00"), balance.getTotalEgresos());
        assertEquals(new java.math.BigDecimal("250.00"), balance.getUtilidad());
        assertEquals(LocalDate.now(), ingresoCaptor.getValue().getFecha());
        assertEquals(LocalDate.now(), egresoCaptor.getValue().getFecha());
        verify(balanceMensualService, org.mockito.Mockito.times(2)).guardarTotalesAutomaticos(balance);
    }

    private Cuenta cuentaSaldada(Long id, Cuenta.TipoCuenta tipo, java.math.BigDecimal importe, Administrador administrador) {
        Cuenta cuenta = cuentaPendiente(id);
        cuenta.setTipo(tipo);
        cuenta.setImporte(importe);
        cuenta.setConcepto("Movimiento de prueba");
        cuenta.setAdministrador(administrador);
        cuenta.setEstado(Cuenta.EstadoCuenta.SALDADA);
        return cuenta;
    }

    private Cuenta cuentaPendiente(Long id) {
        Cuenta cuenta = cuentaConFechas(LocalDate.of(2026, 10, 1), null);
        cuenta.setId(id);
        cuenta.setEstado(Cuenta.EstadoCuenta.PENDIENTE);
        return cuenta;
    }

    private Cuenta cuentaConFechas(LocalDate emision, LocalDate vencimiento) {
        Cuenta cuenta = new Cuenta();
        cuenta.setFechaEmitida(emision);
        cuenta.setFechaVencimiento(vencimiento);
        return cuenta;
    }
}
