package com.backend.depahub.services;

import com.backend.depahub.models.Inquilino;
import com.backend.depahub.repositorys.InquilinoRepository;
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
class InquilinoServiceTest {

    @Mock
    private InquilinoRepository repository;

    private InquilinoService service;

    @BeforeEach
    void setUp() {
        service = new InquilinoService(repository);
    }

    @Test
    void listaLosInquilinosPorInmueble() {
        Inquilino inquilino = new Inquilino();
        when(repository.findByInmuebleId(8L)).thenReturn(List.of(inquilino));

        assertEquals(List.of(inquilino), service.listarPorInmueble(8L));
        verify(repository).findByInmuebleId(8L);
    }
}
