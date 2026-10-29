package com.backend.depahub.services;

import com.backend.depahub.models.Inquilino;
import com.backend.depahub.repositorys.InquilinoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InquilinoService extends BaseCrudService<Inquilino> {

    private final InquilinoRepository repository;
    private final ContratoVencimientoService contratoVencimientoService;

    public InquilinoService(InquilinoRepository repository, ContratoVencimientoService contratoVencimientoService) {
        super(repository);
        this.repository = repository;
        this.contratoVencimientoService = contratoVencimientoService;
    }

    public List<Inquilino> listarPorInmueble(Long inmuebleId) {
        return repository.findByInmuebleId(inmuebleId);
    }

    @Override
    @Transactional
    public Inquilino actualizar(Long id, Inquilino entity) {
        Inquilino actual = obtenerPorId(id);
        if (actual.getEstado() != Inquilino.EstadoInquilino.RETIRADO
                && entity.getEstado() == Inquilino.EstadoInquilino.RETIRADO) {
            contratoVencimientoService.cancelarContratosActivosYRetirar(actual);
            entity.setInmueble(null);
        }
        return super.actualizar(id, entity);
    }

    @Override
    protected void asignarId(Inquilino entity, Long id) {
        entity.setId(id);
    }
}
