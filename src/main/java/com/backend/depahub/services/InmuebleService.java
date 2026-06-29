package com.backend.depahub.services;

import com.backend.depahub.models.Inmueble;
import com.backend.depahub.repositorys.InmuebleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InmuebleService extends BaseCrudService<Inmueble> {

    private final InmuebleRepository repository;

    public InmuebleService(InmuebleRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<Inmueble> listarPorPropiedad(Long propiedadId) {
        return repository.findByPropiedadId(propiedadId);
    }

    @Override
    protected void asignarId(Inmueble entity, Long id) {
        entity.setId(id);
    }
}
