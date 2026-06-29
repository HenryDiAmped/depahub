package com.backend.depahub.repositorys;

import com.backend.depahub.models.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
    List<Cuenta> findByAdministradorId(Long administradorId);

    List<Cuenta> findByInquilinoId(Long inquilinoId);
}
