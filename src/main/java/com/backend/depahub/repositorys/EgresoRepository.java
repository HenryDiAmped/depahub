package com.backend.depahub.repositorys;

import com.backend.depahub.models.Egreso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EgresoRepository extends JpaRepository<Egreso, Long> {
    List<Egreso> findByBalanceMensualId(Long balanceId);
}
