package com.backend.depahub.repositorys;

import com.backend.depahub.models.Inquilino;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquilinoRepository extends JpaRepository<Inquilino, Long> {
    List<Inquilino> findByInmuebleId(Long inmuebleId);

    boolean existsByDni(String dni);

    boolean existsByTelefono(String telefono);

    boolean existsByEmail(String email);
}
