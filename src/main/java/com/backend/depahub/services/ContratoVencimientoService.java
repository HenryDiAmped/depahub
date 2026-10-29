package com.backend.depahub.services;

import com.backend.depahub.models.Contrato;
import com.backend.depahub.models.Inmueble;
import com.backend.depahub.models.Inquilino;
import com.backend.depahub.repositorys.ContratoRepository;
import com.backend.depahub.repositorys.InmuebleRepository;
import com.backend.depahub.repositorys.InquilinoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class ContratoVencimientoService {

    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");

    private final ContratoRepository contratoRepository;
    private final InquilinoRepository inquilinoRepository;
    private final InmuebleRepository inmuebleRepository;

    public ContratoVencimientoService(ContratoRepository contratoRepository,
                                      InquilinoRepository inquilinoRepository,
                                      InmuebleRepository inmuebleRepository) {
        this.contratoRepository = contratoRepository;
        this.inquilinoRepository = inquilinoRepository;
        this.inmuebleRepository = inmuebleRepository;
    }

    /** Se ejecuta todos los días a las 00:05 (hora de Perú) y también procesa vencimientos acumulados. */
    @Scheduled(cron = "0 5 0 * * *", zone = "America/Lima")
    @Transactional
    public void finalizarContratosVencidos() {
        finalizarContratosVencidosHasta(LocalDate.now(ZONA_PERU));
    }

    @Transactional
    public void finalizarContratosVencidosHasta(LocalDate fecha) {
        List<Contrato> vencidos = contratoRepository.findByEstadoAndFechaFinLessThanEqual(
                Contrato.EstadoContrato.ACTIVO, fecha);

        for (Contrato contrato : vencidos) {
            contrato.setEstado(Contrato.EstadoContrato.FINALIZADO);
            contratoRepository.save(contrato);
            retirarYLiberar(contrato.getInquilino());
        }
    }

    /** El retiro manual termina anticipadamente los contratos activos. */
    @Transactional
    public void cancelarContratosActivosYRetirar(Inquilino inquilino) {
        for (Contrato contrato : contratoRepository.findByInquilinoId(inquilino.getId())) {
            if (contrato.getEstado() == Contrato.EstadoContrato.ACTIVO) {
                contrato.setEstado(Contrato.EstadoContrato.CANCELADO);
                contratoRepository.save(contrato);
            }
        }
        retirarYLiberar(inquilino);
    }

    private void retirarYLiberar(Inquilino inquilino) {
        Inmueble inmueble = inquilino.getInmueble();
        inquilino.setEstado(Inquilino.EstadoInquilino.RETIRADO);
        inquilino.setInmueble(null);
        inquilinoRepository.save(inquilino);

        if (inmueble != null) {
            inmueble.setEstado(Inmueble.EstadoInmueble.DISPONIBLE);
            inmuebleRepository.save(inmueble);
        }
    }
}
