package com.backend.depahub.services;

import com.backend.depahub.models.Administrador;
import com.backend.depahub.models.BalanceMensual;
import com.backend.depahub.repositorys.BalanceMensualRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceMensualServiceTest {

    @Mock
    private BalanceMensualRepository repository;

    private BalanceMensualService service;

    @BeforeEach
    void setUp() {
        service = new BalanceMensualService(repository);
    }

    @Test
    void listaLosBalancesPorAdministrador() {
        BalanceMensual balance = new BalanceMensual();
        when(repository.findByAdministradorId(7L)).thenReturn(List.of(balance));

        assertEquals(List.of(balance), service.listarPorAdministrador(7L));
        verify(repository).findByAdministradorId(7L);
    }

    @Test
    void devuelveElBalanceExistenteSinCrearOtro() {
        Administrador administrador = administrador(7L);
        BalanceMensual existente = new BalanceMensual();
        when(repository.findByAdministradorIdAndMesAndAnio(7L, 10, 2026)).thenReturn(Optional.of(existente));

        BalanceMensual resultado = service.obtenerOCrear(administrador, YearMonth.of(2026, 10));

        assertSame(existente, resultado);
        verify(repository, never()).save(any(BalanceMensual.class));
    }

    @Test
    void creaUnBalanceEnCeroSiNoExisteParaElPeriodo() {
        Administrador administrador = administrador(7L);
        when(repository.findByAdministradorIdAndMesAndAnio(7L, 10, 2026)).thenReturn(Optional.empty());
        when(repository.save(any(BalanceMensual.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BalanceMensual creado = service.obtenerOCrear(administrador, YearMonth.of(2026, 10));

        ArgumentCaptor<BalanceMensual> captor = ArgumentCaptor.forClass(BalanceMensual.class);
        verify(repository).save(captor.capture());
        assertSame(creado, captor.getValue());
        assertEquals(administrador, creado.getAdministrador());
        assertEquals(10, creado.getMes());
        assertEquals(2026, creado.getAnio());
        assertEquals(LocalDate.now(), creado.getFechaGeneracion());
        assertEquals(BigDecimal.ZERO, creado.getTotalIngresos());
        assertEquals(BigDecimal.ZERO, creado.getTotalEgresos());
    }

    @Test
    void recalculaLaUtilidadAlGuardarTotalesAutomaticos() {
        BalanceMensual balance = new BalanceMensual();
        balance.setTotalIngresos(new BigDecimal("100.00"));
        balance.setTotalEgresos(new BigDecimal("40.00"));
        balance.setUtilidad(BigDecimal.ZERO);
        when(repository.save(balance)).thenReturn(balance);

        BalanceMensual guardado = service.guardarTotalesAutomaticos(balance);

        assertEquals(new BigDecimal("60.00"), guardado.getUtilidad());
        verify(repository).save(balance);
    }

    @Test
    void asignaLaFechaDeGeneracionAlCrearSiVieneNula() {
        BalanceMensual balance = new BalanceMensual();
        balance.setTotalIngresos(new BigDecimal("10.00"));
        balance.setTotalEgresos(new BigDecimal("3.00"));
        when(repository.save(any(BalanceMensual.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.crear(balance);

        assertEquals(LocalDate.now(), balance.getFechaGeneracion());
        assertEquals(new BigDecimal("7.00"), balance.getUtilidad());
    }

    @Test
    void conservaLaFechaDeGeneracionAlCrearSiYaVieneInformada() {
        LocalDate fecha = LocalDate.of(2026, 1, 15);
        BalanceMensual balance = new BalanceMensual();
        balance.setFechaGeneracion(fecha);
        when(repository.save(any(BalanceMensual.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.crear(balance);

        assertEquals(fecha, balance.getFechaGeneracion());
    }

    @Test
    void copiaLaFechaDeGeneracionAlActualizarSiVieneNula() {
        LocalDate fechaOriginal = LocalDate.of(2026, 3, 1);
        BalanceMensual actual = new BalanceMensual();
        actual.setId(5L);
        actual.setFechaGeneracion(fechaOriginal);
        BalanceMensual cambios = new BalanceMensual();
        cambios.setTotalIngresos(new BigDecimal("20.00"));
        cambios.setTotalEgresos(new BigDecimal("5.00"));
        when(repository.findById(5L)).thenReturn(Optional.of(actual));
        when(repository.save(any(BalanceMensual.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BalanceMensual actualizado = service.actualizar(5L, cambios);

        assertEquals(fechaOriginal, actualizado.getFechaGeneracion());
        assertEquals(new BigDecimal("15.00"), actualizado.getUtilidad());
    }

    private Administrador administrador(Long id) {
        Administrador administrador = new Administrador();
        administrador.setId(id);
        return administrador;
    }
}
