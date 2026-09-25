package com.backend.depahub.services;

import com.backend.depahub.models.Administrador;
import com.backend.depahub.repositorys.AdministradorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

    private Administrador administradorConPassword(String password) {
        Administrador administrador = new Administrador();
        administrador.setEmail("admin@depahub.test");
        administrador.setPassword(password);
        return administrador;
    }
}
