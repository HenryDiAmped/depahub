package com.backend.depahub.repositorys;

import com.backend.depahub.models.Ingreso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngresoRepository extends JpaRepository<Ingreso, Long> {
    List<Ingreso> findByBalanceMensualId(Long balanceId);
}
