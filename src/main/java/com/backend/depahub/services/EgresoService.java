package com.backend.depahub.services;

import com.backend.depahub.models.Egreso;
import com.backend.depahub.repositorys.EgresoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EgresoService extends BaseCrudService<Egreso> {

    private final EgresoRepository repository;

    public EgresoService(EgresoRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<Egreso> listarPorBalance(Long balanceId) {
        return repository.findByBalanceMensualId(balanceId);
    }

    @Override
    protected void asignarId(Egreso entity, Long id) {
        entity.setId(id);
    }
}
