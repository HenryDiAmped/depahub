package com.backend.depahub.services;

import com.backend.depahub.models.Ingreso;
import com.backend.depahub.repositorys.IngresoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngresoServiceTest {

    @Mock
    private IngresoRepository repository;

    private IngresoService service;

    @BeforeEach
    void setUp() {
        service = new IngresoService(repository);
    }

    @Test
    void listaLosIngresosPorBalance() {
        Ingreso ingreso = new Ingreso();
        when(repository.findByBalanceMensualId(21L)).thenReturn(List.of(ingreso));

        assertEquals(List.of(ingreso), service.listarPorBalance(21L));
        verify(repository).findByBalanceMensualId(21L);
    }
}
