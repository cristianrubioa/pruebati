package com.crubio.polizas.riesgo;

public record RiesgoResponse(Long id, String descripcion, EstadoRiesgo estado) {

  public static RiesgoResponse from(Riesgo riesgo) {
    return new RiesgoResponse(riesgo.getId(), riesgo.getDescripcion(), riesgo.getEstado());
  }
}
