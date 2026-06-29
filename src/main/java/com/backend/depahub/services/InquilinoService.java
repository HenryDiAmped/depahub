package com.backend.depahub.services;

import com.backend.depahub.models.Inquilino;
import com.backend.depahub.repositorys.InquilinoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InquilinoService extends BaseCrudService<Inquilino> {

    private final InquilinoRepository repository;

    public InquilinoService(InquilinoRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<Inquilino> listarPorInmueble(Long inmuebleId) {
        return repository.findByInmuebleId(inmuebleId);
    }

    @Override
    protected void asignarId(Inquilino entity, Long id) {
        entity.setId(id);
    }
}
