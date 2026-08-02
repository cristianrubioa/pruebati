package com.crubio.polizas.poliza;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.crubio.polizas.core.CoreNotifier;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PolizaServiceTest {

  @Mock private PolizaRepository polizaRepository;
  @Mock private CoreNotifier coreNotifier;

  private PolizaService polizaService;

  @BeforeEach
  void setUp() {
    polizaService = new PolizaService(polizaRepository, coreNotifier, new BigDecimal("0.05"));
  }

  @Test
  void renovarNotificaCoreTrasConfirmarElCambio() {
    // Setup
    Poliza poliza = new Poliza(TipoPoliza.COLECTIVA, 12, new BigDecimal("100000"));
    when(polizaRepository.findById(1L)).thenReturn(Optional.of(poliza));
    when(polizaRepository.save(any(Poliza.class))).thenReturn(poliza);

    // Action
    PolizaResponse response = polizaService.renovar(1L);

    // Expected
    assertThat(response.estado()).isEqualTo(EstadoPoliza.RENOVADA);
    verify(coreNotifier).notificar("RENOVACION", poliza.getId());
  }

  @Test
  void cancelarNotificaCoreTrasConfirmarElCambio() {
    // Setup
    Poliza poliza = new Poliza(TipoPoliza.COLECTIVA, 12, new BigDecimal("100000"));
    when(polizaRepository.findById(1L)).thenReturn(Optional.of(poliza));
    when(polizaRepository.save(any(Poliza.class))).thenReturn(poliza);

    // Action
    PolizaResponse response = polizaService.cancelar(1L);

    // Expected
    assertThat(response.estado()).isEqualTo(EstadoPoliza.CANCELADA);
    verify(coreNotifier).notificar("CANCELACION", poliza.getId());
  }

  @Test
  void polizaInexistenteLanza404() {
    // Setup
    when(polizaRepository.findById(99L)).thenReturn(Optional.empty());

    // Action / Expected
    assertThatThrownBy(() -> polizaService.cancelar(99L))
        .isInstanceOf(ResponseStatusException.class);
  }
}
