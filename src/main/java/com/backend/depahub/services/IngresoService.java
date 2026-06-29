package com.backend.depahub.services;

import com.backend.depahub.models.Ingreso;
import com.backend.depahub.repositorys.IngresoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngresoService extends BaseCrudService<Ingreso> {

    private final IngresoRepository repository;

    public IngresoService(IngresoRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<Ingreso> listarPorBalance(Long balanceId) {
        return repository.findByBalanceMensualId(balanceId);
    }

    @Override
    protected void asignarId(Ingreso entity, Long id) {
        entity.setId(id);
    }
}
