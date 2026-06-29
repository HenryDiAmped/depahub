package com.backend.depahub.repositorys;

import com.backend.depahub.models.BalanceMensual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BalanceMensualRepository extends JpaRepository<BalanceMensual, Long> {
    List<BalanceMensual> findByAdministradorId(Long administradorId);
}
