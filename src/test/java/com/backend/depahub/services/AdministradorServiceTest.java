package com.backend.depahub.services;

import com.backend.depahub.models.Administrador;
import com.backend.depahub.repositorys.AdministradorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdministradorServiceTest {

    @Mock
    private AdministradorRepository repository;

    private BCryptPasswordEncoder passwordEncoder;
    private AdministradorService service;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        service = new AdministradorService(repository, passwordEncoder);
    }

    @Test
    void cifraLaContraseñaAntesDeCrearUnAdministrador() {
        Administrador administrador = administradorConPassword("secreto");
        when(repository.save(any(Administrador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.crear(administrador);

        assertNotEquals("secreto", administrador.getPassword());
        assertTrue(passwordEncoder.matches("secreto", administrador.getPassword()));
    }

    @Test
    void validaElLoginContraElHashBCrypt() {
        Administrador administrador = administradorConPassword(passwordEncoder.encode("secreto"));
        when(repository.findByEmail("admin@depahub.test")).thenReturn(Optional.of(administrador));

        service.login("admin@depahub.test", "secreto");

        assertTrue(passwordEncoder.matches("secreto", administrador.getPassword()));
    }

    @Test
    void migraLasContraseñasHeredadasEnTextoPlano() {
        Administrador administrador = administradorConPassword("secreto");
        when(repository.findAll()).thenReturn(List.of(administrador));

        service.migrarContraseñasHeredadas();

        verify(repository).saveAll(List.of(administrador));
        assertTrue(passwordEncoder.matches("secreto", administrador.getPassword()));
    }

    @Test
    void rechazaElLoginSiElEmailNoExiste() {
        when(repository.findByEmail("admin@depahub.test")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.login("admin@depahub.test", "secreto"));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void rechazaElLoginSiLaPasswordEsIncorrecta() {
        Administrador administrador = administradorConPassword(passwordEncoder.encode("secreto"));
        when(repository.findByEmail("admin@depahub.test")).thenReturn(Optional.of(administrador));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.login("admin@depahub.test", "otra"));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void rechazaCrearUnAdministradorSinPassword() {
        Administrador administrador = administradorConPassword("   ");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(administrador));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(repository, never()).save(any(Administrador.class));
    }

    @Test
    void rechazaCrearUnAdministradorConEmailDuplicado() {
        Administrador existente = administradorConPassword("secreto");
        existente.setId(1L);
        when(repository.findByEmail("admin@depahub.test")).thenReturn(Optional.of(existente));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(administradorConPassword("secreto")));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(repository, never()).save(any(Administrador.class));
    }

    @Test
    void conservaLaPasswordActualSiAlActualizarVieneVacia() {
        String hashActual = passwordEncoder.encode("secreto");
        Administrador actual = administradorConPassword(hashActual);
        actual.setId(4L);
        Administrador cambios = administradorConPassword("  ");
        when(repository.findById(4L)).thenReturn(Optional.of(actual));
        when(repository.save(any(Administrador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Administrador actualizado = service.actualizar(4L, cambios);

        assertEquals(hashActual, actualizado.getPassword());
    }

    @Test
    void recifraLaPasswordSiAlActualizarVieneUnaNueva() {
        Administrador actual = administradorConPassword(passwordEncoder.encode("secreto"));
        actual.setId(4L);
        Administrador cambios = administradorConPassword("nueva");
        when(repository.findById(4L)).thenReturn(Optional.of(actual));
        when(repository.save(any(Administrador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Administrador actualizado = service.actualizar(4L, cambios);

        assertNotEquals("nueva", actualizado.getPassword());
        assertTrue(passwordEncoder.matches("nueva", actualizado.getPassword()));
    }

    @Test
    void permiteActualizarConservandoElMismoEmail() {
        Administrador actual = administradorConPassword(passwordEncoder.encode("secreto"));
        actual.setId(4L);
        Administrador cambios = administradorConPassword("  ");
        when(repository.findById(4L)).thenReturn(Optional.of(actual));
        when(repository.findByEmail("admin@depahub.test")).thenReturn(Optional.of(actual));
        when(repository.save(any(Administrador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> service.actualizar(4L, cambios));
        verify(repository).save(any(Administrador.class));
    }

    @Test
    void noMigraLasContraseñasQueYaSonBCrypt() {
        Administrador administrador = administradorConPassword(passwordEncoder.encode("secreto"));
        String hashOriginal = administrador.getPassword();
        when(repository.findAll()).thenReturn(List.of(administrador));

        service.migrarContraseñasHeredadas();

        verify(repository, never()).saveAll(any());
        assertEquals(hashOriginal, administrador.getPassword());
    }

    private Administrador administradorConPassword(String password) {
        Administrador administrador = new Administrador();
        administrador.setEmail("admin@depahub.test");
        administrador.setPassword(password);
        return administrador;
    }
}
