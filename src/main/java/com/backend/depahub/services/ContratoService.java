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
        calcularNumeroCuotas(entity);
        if (entity.getFechaRegistro() == null) {
            entity.setFechaRegistro(LocalDate.now());
        }
    }

    @Override
    protected void prepararActualizacion(Contrato entity, Contrato actual) {
        calcularNumeroCuotas(entity);
        if (entity.getFechaRegistro() == null) {
            entity.setFechaRegistro(actual.getFechaRegistro());
        }
    }

    private void calcularNumeroCuotas(Contrato contrato) {
        if (contrato.getFechaInicio() == null || contrato.getFechaFin() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las fechas de inicio y fin son obligatorias");
        }
        if (contrato.getFechaFin().isBefore(contrato.getFechaInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        if (contrato.getFrecuencia() == null || contrato.getFrecuencia() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La frecuencia debe ser mayor que cero");
        }

        int cuotasCompletas = 0;
        LocalDate inicioPeriodo = contrato.getFechaInicio();

        while (!inicioPeriodo.plusMonths(contrato.getFrecuencia()).minusDays(1)
                .isAfter(contrato.getFechaFin())) {
            cuotasCompletas++;
            inicioPeriodo = inicioPeriodo.plusMonths(contrato.getFrecuencia());
        }

        boolean tieneProrrateo = !inicioPeriodo.isAfter(contrato.getFechaFin());
        contrato.setNumeroCuotas(cuotasCompletas + (tieneProrrateo ? 1 : 0));
    }

    @Override
    protected void asignarId(Contrato entity, Long id) {
        entity.setId(id);
    }
}
