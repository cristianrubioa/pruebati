package com.crubio.polizas.core;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

class CoreNotifierTest {

  @Test
  void unaFallaDeRedNoPropagaExcepcion() {
    // Setup: an unreachable base URL simulates the CORE mock being down
    CoreNotifier notifier = new CoreNotifier("http://localhost:1");

    // Action / Expected
    assertThatCode(() -> notifier.notificar("CANCELACION", 1L)).doesNotThrowAnyException();
  }
}
