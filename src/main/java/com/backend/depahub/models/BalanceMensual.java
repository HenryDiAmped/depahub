package com.backend.depahub.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "balances_mensuales", uniqueConstraints =
        @UniqueConstraint(columnNames = {"administrador_id", "mes", "anio"}))
public class BalanceMensual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer mes;

    @Column(nullable = false)
    private Integer anio;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalIngresos = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalEgresos = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal utilidad = BigDecimal.ZERO;

    public void recalcularUtilidad() {
        BigDecimal ingresos = totalIngresos == null ? BigDecimal.ZERO : totalIngresos;
        BigDecimal egresos = totalEgresos == null ? BigDecimal.ZERO : totalEgresos;
        utilidad = ingresos.subtract(egresos);
    }

    @Column(nullable = false)
    private LocalDate fechaGeneracion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "administrador_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Administrador administrador;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMes() {
        return mes;
    }

    public void setMes(Integer mes) {
        this.mes = mes;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public BigDecimal getUtilidad() {
        return utilidad;
    }

    public void setUtilidad(BigDecimal utilidad) {
        this.utilidad = utilidad;
    }

    public LocalDate getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDate fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public BigDecimal getTotalIngresos() {
        return totalIngresos;
    }

    public void setTotalIngresos(BigDecimal totalIngresos) {
        this.totalIngresos = totalIngresos;
        recalcularUtilidad();
    }

    public BigDecimal getTotalEgresos() {
        return totalEgresos;
    }

    public void setTotalEgresos(BigDecimal totalEgresos) {
        this.totalEgresos = totalEgresos;
        recalcularUtilidad();
    }

    public Administrador getAdministrador() {
        return administrador;
    }

    public void setAdministrador(Administrador administrador) {
        this.administrador = administrador;
    }
}
