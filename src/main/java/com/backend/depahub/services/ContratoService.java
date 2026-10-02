package com.backend.depahub.services;

import com.backend.depahub.models.Contrato;
import com.backend.depahub.models.Cuenta;
import com.backend.depahub.repositorys.ContratoRepository;
import com.backend.depahub.repositorys.CuentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class ContratoService extends BaseCrudService<Contrato> {

    private final ContratoRepository repository;
    private final CuentaRepository cuentaRepository;

    public ContratoService(ContratoRepository repository, CuentaRepository cuentaRepository) {
        super(repository);
        this.repository = repository;
        this.cuentaRepository = cuentaRepository;
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
    @Transactional
    public Contrato crear(Contrato entity) {
        Contrato contrato = super.crear(entity);
        cuentaRepository.saveAll(generarCuentasPorCobrar(contrato));
        return contrato;
    }

    @Override
    protected void prepararCreacion(Contrato entity) {
        entity.setNumeroCuotas(generarCalendarioCuotas(entity).size());
        if (entity.getFechaRegistro() == null) {
            entity.setFechaRegistro(LocalDate.now());
        }
    }

    @Override
    protected void prepararActualizacion(Contrato entity, Contrato actual) {
        entity.setNumeroCuotas(generarCalendarioCuotas(entity).size());
        if (entity.getFechaRegistro() == null) {
            entity.setFechaRegistro(actual.getFechaRegistro());
        }
    }

    private List<CuotaProgramada> generarCalendarioCuotas(Contrato contrato) {
        if (contrato.getFechaInicio() == null || contrato.getFechaFin() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las fechas de inicio y fin son obligatorias");
        }
        if (contrato.getFechaFin().isBefore(contrato.getFechaInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        LocalDate finPrimerPeriodo = contrato.getFechaInicio().plusMonths(1).minusDays(1);
        if (contrato.getFechaFin().isBefore(finPrimerPeriodo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El contrato debe tener una duración mínima de un mes");
        }
        if (contrato.getFrecuencia() == null || contrato.getFrecuencia() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La frecuencia debe ser mayor que cero");
        }
        LocalDate finPrimerPeriodoSegunFrecuencia = contrato.getFechaInicio()
                .plusMonths(contrato.getFrecuencia())
                .minusDays(1);
        if (contrato.getFechaFin().isBefore(finPrimerPeriodoSegunFrecuencia)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La frecuencia de pago no es compatible con la duración del contrato");
        }

        List<CuotaProgramada> cuotas = new ArrayList<>();
        LocalDate inicioPeriodo = contrato.getFechaInicio();

        while (!inicioPeriodo.isAfter(contrato.getFechaFin())) {
            LocalDate siguientePeriodo = inicioPeriodo.plusMonths(contrato.getFrecuencia());
            LocalDate finPeriodo = siguientePeriodo.minusDays(1);

            if (!finPeriodo.isAfter(contrato.getFechaFin())) {
                cuotas.add(new CuotaProgramada(siguientePeriodo, contrato.getMontoAlquiler(), false));
                inicioPeriodo = siguientePeriodo;
                continue;
            }

            long diasPeriodoCompleto = ChronoUnit.DAYS.between(inicioPeriodo, siguientePeriodo);
            long diasProrrateados = ChronoUnit.DAYS.between(inicioPeriodo, contrato.getFechaFin()) + 1;
            cuotas.add(new CuotaProgramada(
                    contrato.getFechaFin(),
                    contrato.getMontoAlquiler()
                            .multiply(java.math.BigDecimal.valueOf(diasProrrateados))
                            .divide(java.math.BigDecimal.valueOf(diasPeriodoCompleto), 2, RoundingMode.HALF_UP),
                    true));
            break;
        }

        return cuotas;
    }

    private List<Cuenta> generarCuentasPorCobrar(Contrato contrato) {
        List<Cuenta> cuentas = new ArrayList<>();
        cuentas.add(crearCuenta(
                contrato,
                contrato.getGarantia(),
                "Garantía - Contrato #" + contrato.getId(),
                contrato.getFechaInicio().plusDays(3)));

        List<CuotaProgramada> cuotas = generarCalendarioCuotas(contrato);
        for (int indice = 0; indice < cuotas.size(); indice++) {
            CuotaProgramada cuota = cuotas.get(indice);
            String concepto = cuota.prorrateada()
                    ? "Cuota prorrateada " + (indice + 1) + " de " + cuotas.size() + " - Contrato #" + contrato.getId()
                    : "Cuota " + (indice + 1) + " de " + cuotas.size() + " - Contrato #" + contrato.getId();
            cuentas.add(crearCuenta(contrato, cuota.importe(), concepto, cuota.fechaVencimiento()));
        }
        return cuentas;
    }

    private Cuenta crearCuenta(Contrato contrato, java.math.BigDecimal importe, String concepto, LocalDate vencimiento) {
        Cuenta cuenta = new Cuenta();
        cuenta.setTipo(Cuenta.TipoCuenta.POR_COBRAR);
        cuenta.setEstado(Cuenta.EstadoCuenta.PENDIENTE);
        cuenta.setImporte(importe);
        cuenta.setConcepto(concepto);
        cuenta.setFechaEmitida(contrato.getFechaRegistro());
        cuenta.setFechaVencimiento(vencimiento);
        cuenta.setAdministrador(contrato.getAdministrador());
        cuenta.setInquilino(contrato.getInquilino());
        cuenta.setContrato(contrato);
        return cuenta;
    }

    private record CuotaProgramada(LocalDate fechaVencimiento, java.math.BigDecimal importe, boolean prorrateada) {
    }

    @Override
    protected void asignarId(Contrato entity, Long id) {
        entity.setId(id);
    }
}
