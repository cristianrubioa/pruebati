package com.crubio.polizas.poliza;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.crubio.polizas.common.BusinessRuleException;
import com.crubio.polizas.riesgo.Riesgo;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PolizaTest {

  @Test
  void computaPrimaComoCanonPorMeses() {
    // Setup
    Poliza poliza = new Poliza(TipoPoliza.COLECTIVA, 12, new BigDecimal("100000"));

    // Action / Expected
    assertThat(poliza.getPrima()).isEqualByComparingTo("1200000");
    assertThat(poliza.getEstado()).isEqualTo(EstadoPoliza.ACTIVA);
  }

  @Test
  void polizaIndividualRechazaMasDeUnRiesgo() {
    // Setup
    Poliza poliza = new Poliza(TipoPoliza.INDIVIDUAL, 12, new BigDecimal("100000"));
    poliza.agregarRiesgo(new Riesgo("Incendio"));

    // Action / Expected
    assertThatThrownBy(() -> poliza.agregarRiesgo(new Riesgo("Robo")))
        .isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void renovarIncrementaCanonYPrimaPorIpcYCambiaEstado() {
    // Setup
    Poliza poliza = new Poliza(TipoPoliza.COLECTIVA, 12, new BigDecimal("100000"));

    // Action
    poliza.renovar(new BigDecimal("0.05"));

    // Expected
    assertThat(poliza.getCanonMensual()).isEqualByComparingTo("105000");
    assertThat(poliza.getPrima()).isEqualByComparingTo("1260000");
    assertThat(poliza.getEstado()).isEqualTo(EstadoPoliza.RENOVADA);
  }

  @Test
  void noSePuedeRenovarUnaPolizaCancelada() {
    // Setup
    Poliza poliza = new Poliza(TipoPoliza.COLECTIVA, 12, new BigDecimal("100000"));
    poliza.cancelar();

    // Action / Expected
    assertThatThrownBy(() -> poliza.renovar(new BigDecimal("0.05")))
        .isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void noSePuedeCancelarUnaPolizaYaCancelada() {
    // Setup
    Poliza poliza = new Poliza(TipoPoliza.COLECTIVA, 12, new BigDecimal("100000"));
    poliza.cancelar();

    // Action / Expected
    assertThatThrownBy(poliza::cancelar).isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void cancelarPolizaCancelaTodosSusRiesgosActivos() {
    // Setup
    Poliza poliza = new Poliza(TipoPoliza.COLECTIVA, 12, new BigDecimal("100000"));
    Riesgo riesgo1 = new Riesgo("Incendio");
    Riesgo riesgo2 = new Riesgo("Robo");
    poliza.agregarRiesgo(riesgo1);
    poliza.agregarRiesgo(riesgo2);
    riesgo2.cancelar();

    // Action
    poliza.cancelar();

    // Expected
    assertThat(riesgo1.getEstado().name()).isEqualTo("CANCELADO");
    assertThat(riesgo2.getEstado().name()).isEqualTo("CANCELADO");
  }
}
