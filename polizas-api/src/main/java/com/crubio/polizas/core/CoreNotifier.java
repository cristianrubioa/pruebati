package com.crubio.polizas.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestClient;

@Component
public class CoreNotifier {

  private static final Logger log = LoggerFactory.getLogger(CoreNotifier.class);

  private final RestClient restClient;

  public CoreNotifier(@Value("${polizas.core-mock.base-url}") String baseUrl) {
    this.restClient = RestClient.create(baseUrl);
  }

  public void notificar(String evento, Long polizaId) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              enviar(evento, polizaId);
            }
          });
    } else {
      enviar(evento, polizaId);
    }
  }

  private void enviar(String evento, Long polizaId) {
    try {
      restClient
          .post()
          .uri("/core-mock/evento")
          .body(new CoreEventoRequest(evento, polizaId))
          .retrieve()
          .toBodilessEntity();
    } catch (Exception ex) {
      log.warn("Fallo al notificar evento {} al CORE para poliza {}", evento, polizaId, ex);
    }
  }
}
