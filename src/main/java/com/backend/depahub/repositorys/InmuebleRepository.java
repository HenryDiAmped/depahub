package com.backend.depahub.repositorys;

import com.backend.depahub.models.Inmueble;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InmuebleRepository extends JpaRepository<Inmueble, Long> {
    List<Inmueble> findByPropiedadId(Long propiedadId);

    boolean existsByNombre(String nombre);
}
