package com.backend.depahub.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseCrudServiceTest {

    @Mock
    private JpaRepository<Entidad, Long> repository;

    private ServicioPrueba service;

    @BeforeEach
    void setUp() {
        service = new ServicioPrueba(repository);
    }

    @Test
    void listaTodasLasEntidades() {
        Entidad entidad = new Entidad();
        when(repository.findAll()).thenReturn(List.of(entidad));

        assertEquals(List.of(entidad), service.listar());
        verify(repository).findAll();
    }

    @Test
    void entregaLaEntidadCuandoExiste() {
        Entidad entidad = new Entidad();
        when(repository.findById(4L)).thenReturn(Optional.of(entidad));

        assertSame(entidad, service.obtenerPorId(4L));
    }

    @Test
    void rechazaObtenerUnaEntidadInexistente() {
        when(repository.findById(4L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.obtenerPorId(4L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void preparaYGuardaAlCrear() {
        Entidad entidad = new Entidad();
        when(repository.save(entidad)).thenReturn(entidad);

        Entidad creada = service.crear(entidad);

        assertTrue(service.prepararCreacionLlamado);
        assertSame(entidad, service.entidadPreparada);
        assertSame(entidad, creada);
        verify(repository).save(entidad);
    }

    @Test
    void asignaElIdYPreparaAlActualizar() {
        Entidad actual = new Entidad();
        actual.id = 4L;
        Entidad cambios = new Entidad();
        when(repository.findById(4L)).thenReturn(Optional.of(actual));
        when(repository.save(cambios)).thenReturn(cambios);

        Entidad actualizada = service.actualizar(4L, cambios);

        assertEquals(4L, cambios.id);
        assertSame(cambios, service.entidadPreparada);
        assertSame(actual, service.actualRecibida);
        assertSame(cambios, actualizada);
        verify(repository).save(cambios);
    }

    @Test
    void eliminaDespuesDeComprobarQueExiste() {
        Entidad entidad = new Entidad();
        when(repository.findById(4L)).thenReturn(Optional.of(entidad));

        service.eliminar(4L);

        verify(repository).deleteById(4L);
    }

    @Test
    void noEliminaSiLaEntidadNoExiste() {
        when(repository.findById(4L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.eliminar(4L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(repository, never()).deleteById(4L);
    }

    private static class Entidad {
        private Long id;
    }

    private static class ServicioPrueba extends BaseCrudService<Entidad> {
        private boolean prepararCreacionLlamado;
        private Entidad entidadPreparada;
        private Entidad actualRecibida;

        private ServicioPrueba(JpaRepository<Entidad, Long> repository) {
            super(repository);
        }

        @Override
        protected void prepararCreacion(Entidad entity) {
            prepararCreacionLlamado = true;
            entidadPreparada = entity;
        }

        @Override
        protected void prepararActualizacion(Entidad entity, Entidad actual) {
            entidadPreparada = entity;
            actualRecibida = actual;
        }

        @Override
        protected void asignarId(Entidad entity, Long id) {
            entity.id = id;
        }
    }
}
