package com.backend.depahub.repositorys;

import com.backend.depahub.models.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
    Optional<Administrador> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByDni(String dni);

    boolean existsByTelefono(String telefono);
}
