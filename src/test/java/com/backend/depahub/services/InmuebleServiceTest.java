package com.backend.depahub.services;

import com.backend.depahub.models.Inmueble;
import com.backend.depahub.repositorys.InmuebleRepository;
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
class InmuebleServiceTest {

    @Mock
    private InmuebleRepository repository;

    private InmuebleService service;

    @BeforeEach
    void setUp() {
        service = new InmuebleService(repository);
    }

    @Test
    void listaLosInmueblesPorPropiedad() {
        Inmueble inmueble = new Inmueble();
        when(repository.findByPropiedadId(5L)).thenReturn(List.of(inmueble));

        assertEquals(List.of(inmueble), service.listarPorPropiedad(5L));
        verify(repository).findByPropiedadId(5L);
    }
}
