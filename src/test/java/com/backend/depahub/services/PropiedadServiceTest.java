package com.backend.depahub.services;

import com.backend.depahub.models.Propiedad;
import com.backend.depahub.repositorys.PropiedadRepository;
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
class PropiedadServiceTest {

    @Mock
    private PropiedadRepository repository;

    private PropiedadService service;

    @BeforeEach
    void setUp() {
        service = new PropiedadService(repository);
    }

    @Test
    void listaLasPropiedadesPorAdministrador() {
        Propiedad propiedad = new Propiedad();
        when(repository.findByAdministradorId(7L)).thenReturn(List.of(propiedad));

        assertEquals(List.of(propiedad), service.listarPorAdministrador(7L));
        verify(repository).findByAdministradorId(7L);
    }
}
