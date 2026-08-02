package com.crubio.polizas.riesgo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.crubio.polizas.common.BusinessRuleException;
import com.crubio.polizas.core.CoreNotifier;
import com.crubio.polizas.poliza.Poliza;
import com.crubio.polizas.poliza.PolizaRepository;
import com.crubio.polizas.poliza.TipoPoliza;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RiesgoServiceTest {

  @Mock private PolizaRepository polizaRepository;
  @Mock private RiesgoRepository riesgoRepository;
  @Mock private CoreNotifier coreNotifier;

  private RiesgoService riesgoService;

  @BeforeEach
  void setUp() {
    riesgoService = new RiesgoService(polizaRepository, riesgoRepository, coreNotifier);
  }

  @Test
  void rechazaAgregarRiesgoAPolizaIndividualYNoNotificaCore() {
    // Setup
    Poliza individual = new Poliza(TipoPoliza.INDIVIDUAL, 12, new BigDecimal("100000"));
    individual.agregarRiesgo(new Riesgo("Incendio"));
    when(polizaRepository.findById(1L)).thenReturn(Optional.of(individual));

    // Action / Expected
    assertThatThrownBy(() -> riesgoService.agregar(1L, new RiesgoRequest("Robo")))
        .isInstanceOf(BusinessRuleException.class);
    verifyNoInteractions(coreNotifier);
  }

  @Test
  void agregaRiesgoAPolizaColectivaYNotificaCore() {
    // Setup
    Poliza colectiva = new Poliza(TipoPoliza.COLECTIVA, 12, new BigDecimal("100000"));
    when(polizaRepository.findById(1L)).thenReturn(Optional.of(colectiva));
    when(riesgoRepository.save(any(Riesgo.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Action
    RiesgoResponse response = riesgoService.agregar(1L, new RiesgoRequest("Incendio"));

    // Expected
    assertThat(response.descripcion()).isEqualTo("Incendio");
    verify(coreNotifier).notificar("AGREGAR_RIESGO", colectiva.getId());
  }

  @Test
  void cancelarRiesgoNotificaCoreConIdDeLaPoliza() {
    // Setup
    Poliza poliza = new Poliza(TipoPoliza.COLECTIVA, 12, new BigDecimal("100000"));
    Riesgo riesgo = new Riesgo("Incendio");
    poliza.agregarRiesgo(riesgo);
    when(riesgoRepository.findById(5L)).thenReturn(Optional.of(riesgo));
    when(riesgoRepository.save(any(Riesgo.class))).thenReturn(riesgo);

    // Action
    RiesgoResponse response = riesgoService.cancelar(5L);

    // Expected
    assertThat(response.estado()).isEqualTo(EstadoRiesgo.CANCELADO);
    verify(coreNotifier).notificar("CANCELAR_RIESGO", poliza.getId());
  }
}
