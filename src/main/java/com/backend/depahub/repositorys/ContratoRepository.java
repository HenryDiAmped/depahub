package com.backend.depahub.repositorys;

import com.backend.depahub.models.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {
    List<Contrato> findByAdministradorId(Long administradorId);

    List<Contrato> findByInquilinoId(Long inquilinoId);
}
