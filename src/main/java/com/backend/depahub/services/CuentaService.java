package com.backend.depahub.services;

import com.backend.depahub.models.Cuenta;
import com.backend.depahub.repositorys.CuentaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CuentaService extends BaseCrudService<Cuenta> {

    private final CuentaRepository repository;

    public CuentaService(CuentaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<Cuenta> listarPorAdministrador(Long administradorId) {
        return repository.findByAdministradorId(administradorId);
    }

    public List<Cuenta> listarPorInquilino(Long inquilinoId) {
        return repository.findByInquilinoId(inquilinoId);
    }

    @Override
    protected void asignarId(Cuenta entity, Long id) {
        entity.setId(id);
    }
}
