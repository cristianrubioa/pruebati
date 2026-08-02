package com.crubio.polizas.poliza;

import com.crubio.polizas.riesgo.RiesgoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

public record PolizaRequest(
    @NotNull TipoPoliza tipo,
    @NotNull @Positive Integer vigenciaMeses,
    @NotNull @Positive BigDecimal canonMensual,
    @NotEmpty @Valid List<RiesgoRequest> riesgos) {}
