package com.crubio.polizas.poliza;

import com.crubio.polizas.riesgo.RiesgoResponse;
import java.math.BigDecimal;
import java.util.List;

public record PolizaResponse(
    Long id,
    TipoPoliza tipo,
    EstadoPoliza estado,
    Integer vigenciaMeses,
    BigDecimal canonMensual,
    BigDecimal prima,
    List<RiesgoResponse> riesgos) {

  public static PolizaResponse from(Poliza poliza) {
    return new PolizaResponse(
        poliza.getId(),
        poliza.getTipo(),
        poliza.getEstado(),
        poliza.getVigenciaMeses(),
        poliza.getCanonMensual(),
        poliza.getPrima(),
        poliza.getRiesgos().stream().map(RiesgoResponse::from).toList());
  }
}
