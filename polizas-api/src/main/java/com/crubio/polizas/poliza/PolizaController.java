package com.crubio.polizas.poliza;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PolizaController {

  private final PolizaService polizaService;

  public PolizaController(PolizaService polizaService) {
    this.polizaService = polizaService;
  }

  @PostMapping("/polizas")
  @ResponseStatus(HttpStatus.CREATED)
  public PolizaResponse crear(@Valid @RequestBody PolizaRequest request) {
    return polizaService.crear(request);
  }

  @GetMapping("/polizas")
  public List<PolizaResponse> listar(
      @RequestParam(required = false) TipoPoliza tipo,
      @RequestParam(required = false) EstadoPoliza estado) {
    return polizaService.listar(tipo, estado);
  }

  @PostMapping("/polizas/{id}/renovar")
  public PolizaResponse renovar(@PathVariable Long id) {
    return polizaService.renovar(id);
  }

  @PostMapping("/polizas/{id}/cancelar")
  public PolizaResponse cancelar(@PathVariable Long id) {
    return polizaService.cancelar(id);
  }
}
