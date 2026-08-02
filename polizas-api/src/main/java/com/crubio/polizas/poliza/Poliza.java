package com.crubio.polizas.poliza;

import com.crubio.polizas.common.BusinessRuleException;
import com.crubio.polizas.riesgo.EstadoRiesgo;
import com.crubio.polizas.riesgo.Riesgo;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Poliza {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  private TipoPoliza tipo;

  @Enumerated(EnumType.STRING)
  private EstadoPoliza estado;

  private Integer vigenciaMeses;

  private BigDecimal canonMensual;

  private BigDecimal prima;

  @OneToMany(mappedBy = "poliza", cascade = CascadeType.ALL, orphanRemoval = false)
  private List<Riesgo> riesgos = new ArrayList<>();

  protected Poliza() {}

  public Poliza(TipoPoliza tipo, Integer vigenciaMeses, BigDecimal canonMensual) {
    this.tipo = tipo;
    this.vigenciaMeses = vigenciaMeses;
    this.canonMensual = canonMensual;
    this.prima = canonMensual.multiply(BigDecimal.valueOf(vigenciaMeses));
    this.estado = EstadoPoliza.ACTIVA;
  }

  public void agregarRiesgo(Riesgo riesgo) {
    if (tipo == TipoPoliza.INDIVIDUAL && !riesgos.isEmpty()) {
      throw new BusinessRuleException("Una póliza individual solo puede tener 1 riesgo");
    }
    riesgo.setPoliza(this);
    riesgos.add(riesgo);
  }

  public void renovar(BigDecimal ipcRate) {
    if (estado == EstadoPoliza.CANCELADA) {
      throw new BusinessRuleException("No se puede renovar una póliza cancelada");
    }
    this.canonMensual = canonMensual.add(canonMensual.multiply(ipcRate));
    this.prima = canonMensual.multiply(BigDecimal.valueOf(vigenciaMeses));
    this.estado = EstadoPoliza.RENOVADA;
  }

  public void cancelar() {
    if (estado == EstadoPoliza.CANCELADA) {
      throw new BusinessRuleException("La póliza ya está cancelada");
    }
    this.estado = EstadoPoliza.CANCELADA;
    riesgos.stream()
        .filter(riesgo -> riesgo.getEstado() != EstadoRiesgo.CANCELADO)
        .forEach(Riesgo::cancelar);
  }

  public Long getId() {
    return id;
  }

  public TipoPoliza getTipo() {
    return tipo;
  }

  public EstadoPoliza getEstado() {
    return estado;
  }

  public Integer getVigenciaMeses() {
    return vigenciaMeses;
  }

  public BigDecimal getCanonMensual() {
    return canonMensual;
  }

  public BigDecimal getPrima() {
    return prima;
  }

  public List<Riesgo> getRiesgos() {
    return riesgos;
  }
}
