package com.crubio.polizas.riesgo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.crubio.polizas.common.BusinessRuleException;
import org.junit.jupiter.api.Test;

class RiesgoTest {

  @Test
  void naceEnEstadoActivo() {
    // Setup / Action
    Riesgo riesgo = new Riesgo("Incendio");

    // Expected
    assertThat(riesgo.getEstado()).isEqualTo(EstadoRiesgo.ACTIVO);
  }

  @Test
  void cancelarCambiaEstadoACancelado() {
    // Setup
    Riesgo riesgo = new Riesgo("Incendio");

    // Action
    riesgo.cancelar();

    // Expected
    assertThat(riesgo.getEstado()).isEqualTo(EstadoRiesgo.CANCELADO);
  }

  @Test
  void rechazaCancelarUnRiesgoYaCancelado() {
    // Setup
    Riesgo riesgo = new Riesgo("Incendio");
    riesgo.cancelar();

    // Action / Expected
    assertThatThrownBy(riesgo::cancelar).isInstanceOf(BusinessRuleException.class);
  }
}
