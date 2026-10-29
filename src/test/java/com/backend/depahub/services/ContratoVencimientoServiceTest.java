package com.backend.depahub.services;

import com.backend.depahub.models.Contrato;
import com.backend.depahub.models.Inmueble;
import com.backend.depahub.models.Inquilino;
import com.backend.depahub.repositorys.ContratoRepository;
import com.backend.depahub.repositorys.InmuebleRepository;
import com.backend.depahub.repositorys.InquilinoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContratoVencimientoServiceTest {

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private InquilinoRepository inquilinoRepository;

    @Mock
    private InmuebleRepository inmuebleRepository;

    private ContratoVencimientoService service;

    @BeforeEach
    void setUp() {
        service = new ContratoVencimientoService(contratoRepository, inquilinoRepository, inmuebleRepository);
    }

    @Test
    void alVencerUnContratoLoFinalizaRetiraAlInquilinoYLiberaElInmueble() {
        LocalDate hoy = LocalDate.of(2026, 10, 29);
        Inmueble inmueble = new Inmueble();
        inmueble.setEstado(Inmueble.EstadoInmueble.OCUPADO);
        Inquilino inquilino = new Inquilino();
        inquilino.setEstado(Inquilino.EstadoInquilino.ACTIVO);
        inquilino.setInmueble(inmueble);
        Contrato contrato = new Contrato();
        contrato.setEstado(Contrato.EstadoContrato.ACTIVO);
        contrato.setInquilino(inquilino);
        when(contratoRepository.findByEstadoAndFechaFinLessThanEqual(Contrato.EstadoContrato.ACTIVO, hoy))
                .thenReturn(List.of(contrato));

        service.finalizarContratosVencidosHasta(hoy);

        assertEquals(Contrato.EstadoContrato.FINALIZADO, contrato.getEstado());
        assertEquals(Inquilino.EstadoInquilino.RETIRADO, inquilino.getEstado());
        assertEquals(Inmueble.EstadoInmueble.DISPONIBLE, inmueble.getEstado());
        assertEquals(null, inquilino.getInmueble());
        verify(contratoRepository).save(contrato);
        verify(inquilinoRepository).save(inquilino);
        verify(inmuebleRepository).save(inmueble);
    }

    @Test
    void elRetiroManualCancelaSoloLosContratosActivos() {
        Inquilino inquilino = new Inquilino();
        inquilino.setId(7L);
        Contrato activo = new Contrato();
        activo.setEstado(Contrato.EstadoContrato.ACTIVO);
        Contrato finalizado = new Contrato();
        finalizado.setEstado(Contrato.EstadoContrato.FINALIZADO);
        when(contratoRepository.findByInquilinoId(7L)).thenReturn(List.of(activo, finalizado));

        service.cancelarContratosActivosYRetirar(inquilino);

        assertEquals(Contrato.EstadoContrato.CANCELADO, activo.getEstado());
        assertEquals(Contrato.EstadoContrato.FINALIZADO, finalizado.getEstado());
        verify(contratoRepository).save(activo);
    }
}
