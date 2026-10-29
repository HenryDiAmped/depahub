package com.backend.depahub.services;

import com.backend.depahub.models.Inquilino;
import com.backend.depahub.repositorys.InquilinoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InquilinoServiceTest {

    @Mock
    private InquilinoRepository repository;

    @Mock
    private ContratoVencimientoService contratoVencimientoService;

    private InquilinoService service;

    @BeforeEach
    void setUp() {
        service = new InquilinoService(repository, contratoVencimientoService);
    }

    @Test
    void listaLosInquilinosPorInmueble() {
        Inquilino inquilino = new Inquilino();
        when(repository.findByInmuebleId(8L)).thenReturn(List.of(inquilino));

        assertEquals(List.of(inquilino), service.listarPorInmueble(8L));
        verify(repository).findByInmuebleId(8L);
    }

    @Test
    void alRetirarManualmenteCancelaLosContratosActivosDelInquilino() {
        Inquilino actual = new Inquilino();
        actual.setId(4L);
        actual.setEstado(Inquilino.EstadoInquilino.ACTIVO);
        Inquilino retiro = new Inquilino();
        retiro.setEstado(Inquilino.EstadoInquilino.RETIRADO);
        when(repository.findById(4L)).thenReturn(Optional.of(actual));
        when(repository.save(retiro)).thenReturn(retiro);

        service.actualizar(4L, retiro);

        verify(contratoVencimientoService).cancelarContratosActivosYRetirar(actual);
    }
}
