package com.backend.depahub.repositorys;

import com.backend.depahub.models.BalanceMensual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BalanceMensualRepository extends JpaRepository<BalanceMensual, Long> {
    List<BalanceMensual> findByAdministradorId(Long administradorId);

    Optional<BalanceMensual> findByAdministradorIdAndMesAndAnio(Long administradorId, Integer mes, Integer anio);
}
