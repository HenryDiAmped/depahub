package com.backend.depahub.controllers;

import com.backend.depahub.services.BaseCrudService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public abstract class BaseCrudController<T> {

    private final BaseCrudService<T> service;

    protected BaseCrudController(BaseCrudService<T> service) {
        this.service = service;
    }

    @GetMapping
    public List<T> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public T obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<T> crear(@RequestBody T entity) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(entity));
    }

    @PutMapping("/{id}")
    public T actualizar(@PathVariable Long id, @RequestBody T entity) {
        return service.actualizar(id, entity);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
