package com.crubio.polizas.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CoreMockController {

  private static final Logger log = LoggerFactory.getLogger(CoreMockController.class);

  @PostMapping("/core-mock/evento")
  public void recibirEvento(@RequestBody CoreEventoRequest evento) {
    log.info("CORE mock: evento={} polizaId={} recibido", evento.evento(), evento.polizaId());
  }
}
