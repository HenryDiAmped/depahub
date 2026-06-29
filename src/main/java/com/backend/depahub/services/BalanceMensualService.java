package com.backend.depahub.services;

import com.backend.depahub.models.BalanceMensual;
import com.backend.depahub.repositorys.BalanceMensualRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BalanceMensualService extends BaseCrudService<BalanceMensual> {

    private final BalanceMensualRepository repository;

    public BalanceMensualService(BalanceMensualRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<BalanceMensual> listarPorAdministrador(Long administradorId) {
        return repository.findByAdministradorId(administradorId);
    }

    @Override
    protected void prepararCreacion(BalanceMensual entity) {
        if (entity.getFechaGeneracion() == null) {
            entity.setFechaGeneracion(LocalDate.now());
        }
        entity.recalcularUtilidad();
    }

    @Override
    protected void prepararActualizacion(BalanceMensual entity, BalanceMensual actual) {
        if (entity.getFechaGeneracion() == null) {
            entity.setFechaGeneracion(actual.getFechaGeneracion());
        }
        entity.recalcularUtilidad();
    }

    @Override
    protected void asignarId(BalanceMensual entity, Long id) {
        entity.setId(id);
    }
}
