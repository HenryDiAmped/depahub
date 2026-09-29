package com.backend.depahub.services;

import com.backend.depahub.models.Contrato;
import com.backend.depahub.repositorys.ContratoRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class ContratoService extends BaseCrudService<Contrato> {

    private final ContratoRepository repository;

    public ContratoService(ContratoRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<Contrato> listarPorAdministrador(Long administradorId) {
        return repository.findByAdministradorId(administradorId);
    }

    public List<Contrato> listarPorInquilino(Long inquilinoId) {
        return repository.findByInquilinoId(inquilinoId);
    }

    public Contrato obtenerParaAdministrador(Long id, String emailAdministrador) {
        Contrato contrato = obtenerPorId(id);
        if (!contrato.getAdministrador().getEmail().equals(emailAdministrador)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a este contrato");
        }
        return contrato;
    }

    @Override
    protected void prepararCreacion(Contrato entity) {
        if (entity.getFechaRegistro() == null) {
            entity.setFechaRegistro(LocalDate.now());
        }
    }

    @Override
    protected void prepararActualizacion(Contrato entity, Contrato actual) {
        if (entity.getFechaRegistro() == null) {
            entity.setFechaRegistro(actual.getFechaRegistro());
        }
    }

    @Override
    protected void asignarId(Contrato entity, Long id) {
        entity.setId(id);
    }
}
