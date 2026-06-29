package com.backend.depahub.services;

import com.backend.depahub.models.Administrador;
import com.backend.depahub.repositorys.AdministradorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class AdministradorService extends BaseCrudService<Administrador> {

    private final AdministradorRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AdministradorService(AdministradorRepository repository, PasswordEncoder passwordEncoder) {
        super(repository);
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public Administrador login(String email, String password) {
        Administrador administrador = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas"));
        if (!passwordEncoder.matches(password, administrador.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
        }
        return administrador;
    }

    @Override
    protected void prepararCreacion(Administrador entity) {
        validarEmailDisponible(entity.getEmail(), null);
        if (entity.getPassword() == null || entity.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La password es obligatoria");
        }
        entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        if (entity.getFechaRegistro() == null) {
            entity.setFechaRegistro(LocalDate.now());
        }
        if (entity.getUtilidadTotal() == null) {
            entity.setUtilidadTotal(BigDecimal.ZERO);
        }
    }

    @Override
    protected void prepararActualizacion(Administrador entity, Administrador actual) {
        validarEmailDisponible(entity.getEmail(), actual.getId());
        if (entity.getPassword() == null || entity.getPassword().isBlank()) {
            entity.setPassword(actual.getPassword());
        } else {
            entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        }
        if (entity.getFechaRegistro() == null) {
            entity.setFechaRegistro(actual.getFechaRegistro());
        }
        if (entity.getUtilidadTotal() == null) {
            entity.setUtilidadTotal(actual.getUtilidadTotal());
        }
    }

    private void validarEmailDisponible(String email, Long idActual) {
        repository.findByEmail(email).ifPresent(administrador -> {
            if (idActual == null || !administrador.getId().equals(idActual)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya esta registrado");
            }
        });
    }

    @Override
    protected void asignarId(Administrador entity, Long id) {
        entity.setId(id);
    }
}
