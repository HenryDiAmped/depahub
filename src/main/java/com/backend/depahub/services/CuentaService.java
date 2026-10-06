package com.backend.depahub.services;

import com.backend.depahub.models.BalanceMensual;
import com.backend.depahub.models.Cuenta;
import com.backend.depahub.models.Egreso;
import com.backend.depahub.models.Ingreso;
import com.backend.depahub.repositorys.CuentaRepository;
import com.backend.depahub.repositorys.EgresoRepository;
import com.backend.depahub.repositorys.IngresoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class CuentaService extends BaseCrudService<Cuenta> {

    private final CuentaRepository repository;
    private final BalanceMensualService balanceMensualService;
    private final IngresoRepository ingresoRepository;
    private final EgresoRepository egresoRepository;

    public CuentaService(CuentaRepository repository, BalanceMensualService balanceMensualService,
                         IngresoRepository ingresoRepository, EgresoRepository egresoRepository) {
        super(repository);
        this.repository = repository;
        this.balanceMensualService = balanceMensualService;
        this.ingresoRepository = ingresoRepository;
        this.egresoRepository = egresoRepository;
    }

    public List<Cuenta> listarPorAdministrador(Long administradorId) {
        return repository.findByAdministradorId(administradorId);
    }

    public List<Cuenta> listarPorInquilino(Long inquilinoId) {
        return repository.findByInquilinoId(inquilinoId);
    }

    @Override
    @Transactional
    public Cuenta actualizar(Long id, Cuenta entity) {
        Cuenta actual = obtenerPorId(id);
        boolean seEstabaSaldando = actual.getEstado() != Cuenta.EstadoCuenta.SALDADA
                && entity.getEstado() == Cuenta.EstadoCuenta.SALDADA;
        asignarId(entity, id);
        prepararActualizacion(entity, actual);
        Cuenta cuentaActualizada = repository.save(entity);

        if (seEstabaSaldando) {
            registrarMovimientoEnBalance(cuentaActualizada);
        }
        return cuentaActualizada;
    }

    private void registrarMovimientoEnBalance(Cuenta cuenta) {
        LocalDate hoy = LocalDate.now();
        BalanceMensual balance = balanceMensualService.obtenerOCrear(cuenta.getAdministrador(), YearMonth.from(hoy));

        if (cuenta.getTipo() == Cuenta.TipoCuenta.POR_COBRAR) {
            Ingreso ingreso = new Ingreso();
            ingreso.setImporte(cuenta.getImporte());
            ingreso.setConcepto(cuenta.getConcepto());
            ingreso.setFecha(hoy);
            ingreso.setBalanceMensual(balance);
            ingresoRepository.save(ingreso);
            balance.setTotalIngresos(balance.getTotalIngresos().add(cuenta.getImporte()));
        } else {
            Egreso egreso = new Egreso();
            egreso.setImporte(cuenta.getImporte());
            egreso.setConcepto(cuenta.getConcepto());
            egreso.setFecha(hoy);
            egreso.setBalanceMensual(balance);
            egresoRepository.save(egreso);
            balance.setTotalEgresos(balance.getTotalEgresos().add(cuenta.getImporte()));
        }
        balanceMensualService.guardarTotalesAutomaticos(balance);
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
