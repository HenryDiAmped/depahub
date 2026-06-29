package com.backend.depahub.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cuentas")
public class Cuenta {

    public enum TipoCuenta {
        POR_PAGAR,
        POR_COBRAR
    }

    public enum EstadoCuenta {
        PENDIENTE,
        SALDADA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoCuenta tipo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal importe;

    @Column(nullable = false, length = 45)
    private String concepto;

    @Column(nullable = false)
    private LocalDate fechaEmitida;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoCuenta estado = EstadoCuenta.PENDIENTE;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "administrador_id", nullable = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private Administrador administrador;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "inquilino_id", nullable = false)
    @JsonIgnoreProperties({"inmueble"})
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private Inquilino inquilino;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoCuenta getTipo() {
        return tipo;
    }

    public void setTipo(TipoCuenta tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public LocalDate getFechaEmitida() {
        return fechaEmitida;
    }

    public void setFechaEmitida(LocalDate fechaEmitida) {
        this.fechaEmitida = fechaEmitida;
    }

    public EstadoCuenta getEstado() {
        return estado;
    }

    public void setEstado(EstadoCuenta estado) {
        this.estado = estado;
    }

    public Administrador getAdministrador() {
        return administrador;
    }

    public void setAdministrador(Administrador administrador) {
        this.administrador = administrador;
    }

    public Inquilino getInquilino() {
        return inquilino;
    }

    public void setInquilino(Inquilino inquilino) {
        this.inquilino = inquilino;
    }
}
