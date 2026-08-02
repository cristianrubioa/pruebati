package com.crubio.polizas.riesgo;

import jakarta.validation.constraints.NotBlank;

public record RiesgoRequest(@NotBlank String descripcion) {}
