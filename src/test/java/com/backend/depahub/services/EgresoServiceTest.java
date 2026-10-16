package com.backend.depahub.services;

import com.backend.depahub.models.Egreso;
import com.backend.depahub.repositorys.EgresoRepository;
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
class EgresoServiceTest {

    @Mock
    private EgresoRepository repository;

    private EgresoService service;

    @BeforeEach
    void setUp() {
        service = new EgresoService(repository);
    }

    @Test
    void listaLosEgresosPorBalance() {
        Egreso egreso = new Egreso();
        when(repository.findByBalanceMensualId(21L)).thenReturn(List.of(egreso));

        assertEquals(List.of(egreso), service.listarPorBalance(21L));
        verify(repository).findByBalanceMensualId(21L);
    }
}
