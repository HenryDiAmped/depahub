package com.backend.depahub.services;

import com.backend.depahub.models.BalanceMensual;
import com.backend.depahub.models.Administrador;
import com.backend.depahub.repositorys.BalanceMensualRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
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

    /** Obtiene el balance del período o lo crea con sus totales en cero. */
    public BalanceMensual obtenerOCrear(Administrador administrador, YearMonth periodo) {
        return repository.findByAdministradorIdAndMesAndAnio(
                        administrador.getId(), periodo.getMonthValue(), periodo.getYear())
                .orElseGet(() -> {
                    BalanceMensual balance = new BalanceMensual();
                    balance.setAdministrador(administrador);
                    balance.setMes(periodo.getMonthValue());
                    balance.setAnio(periodo.getYear());
                    balance.setFechaGeneracion(LocalDate.now());
                    balance.setTotalIngresos(java.math.BigDecimal.ZERO);
                    balance.setTotalEgresos(java.math.BigDecimal.ZERO);
                    return repository.save(balance);
                });
    }

    /** Persiste los acumulados calculados por un evento automático. */
    public BalanceMensual guardarTotalesAutomaticos(BalanceMensual balance) {
        balance.recalcularUtilidad();
        return repository.save(balance);
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
