package com.backend.depahub.services;

import com.backend.depahub.models.Propiedad;
import com.backend.depahub.repositorys.PropiedadRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropiedadService extends BaseCrudService<Propiedad> {

    private final PropiedadRepository repository;

    public PropiedadService(PropiedadRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<Propiedad> listarPorAdministrador(Long administradorId) {
        return repository.findByAdministradorId(administradorId);
    }

    @Override
    protected void asignarId(Propiedad entity, Long id) {
        entity.setId(id);
    }
}
