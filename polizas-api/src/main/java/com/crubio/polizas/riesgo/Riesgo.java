package com.crubio.polizas.riesgo;

import com.crubio.polizas.common.BusinessRuleException;
import com.crubio.polizas.poliza.Poliza;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Riesgo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String descripcion;

  @Enumerated(EnumType.STRING)
  private EstadoRiesgo estado;

  @ManyToOne
  @JoinColumn(name = "poliza_id")
  private Poliza poliza;

  protected Riesgo() {}

  public Riesgo(String descripcion) {
    this.descripcion = descripcion;
    this.estado = EstadoRiesgo.ACTIVO;
  }

  public Long getId() {
    return id;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public EstadoRiesgo getEstado() {
    return estado;
  }

  public Poliza getPoliza() {
    return poliza;
  }

  public void setPoliza(Poliza poliza) {
    this.poliza = poliza;
  }

  public void cancelar() {
    if (estado == EstadoRiesgo.CANCELADO) {
      throw new BusinessRuleException("El riesgo ya está cancelado");
    }
    this.estado = EstadoRiesgo.CANCELADO;
  }
}
