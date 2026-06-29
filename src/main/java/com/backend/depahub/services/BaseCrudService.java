package com.backend.depahub.services;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

public abstract class BaseCrudService<T> {

    private final JpaRepository<T, Long> repository;

    protected BaseCrudService(JpaRepository<T, Long> repository) {
        this.repository = repository;
    }

    public List<T> listar() {
        return repository.findAll();
    }

    public T obtenerPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro no encontrado"));
    }

    public T crear(T entity) {
        prepararCreacion(entity);
        return repository.save(entity);
    }

    public T actualizar(Long id, T entity) {
        T actual = obtenerPorId(id);
        asignarId(entity, id);
        prepararActualizacion(entity, actual);
        return repository.save(entity);
    }

    public void eliminar(Long id) {
        obtenerPorId(id);
        repository.deleteById(id);
    }

    protected void prepararCreacion(T entity) {
    }

    protected void prepararActualizacion(T entity, T actual) {
    }

    protected abstract void asignarId(T entity, Long id);
}
