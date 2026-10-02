package com.backend.depahub.services;

import com.backend.depahub.models.Cuenta;
import com.backend.depahub.repositorys.CuentaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
    protected void prepararCreacion(Cuenta entity) {
        validarVencimiento(entity);
    }

    @Override
    protected void prepararActualizacion(Cuenta entity, Cuenta actual) {
        validarVencimiento(entity);
    }

    private void validarVencimiento(Cuenta cuenta) {
        if (cuenta.getFechaVencimiento() != null && cuenta.getFechaEmitida() != null
                && cuenta.getFechaVencimiento().isBefore(cuenta.getFechaEmitida())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha de vencimiento no puede ser anterior a la fecha de emisión");
        }
    }

    @Override
    protected void asignarId(Cuenta entity, Long id) {
        entity.setId(id);
    }
}
