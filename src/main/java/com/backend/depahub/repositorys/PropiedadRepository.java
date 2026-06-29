package com.backend.depahub.repositorys;

import com.backend.depahub.models.Propiedad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropiedadRepository extends JpaRepository<Propiedad, Long> {
    List<Propiedad> findByAdministradorId(Long administradorId);

    boolean existsByNombre(String nombre);
}
