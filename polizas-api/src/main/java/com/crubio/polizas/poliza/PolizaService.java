package com.crubio.polizas.poliza;

import com.crubio.polizas.core.CoreNotifier;
import com.crubio.polizas.riesgo.Riesgo;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PolizaService {

  private final PolizaRepository polizaRepository;
  private final CoreNotifier coreNotifier;
  private final BigDecimal ipcRate;

  public PolizaService(
      PolizaRepository polizaRepository,
      CoreNotifier coreNotifier,
      @Value("${polizas.ipc-rate}") BigDecimal ipcRate) {
    this.polizaRepository = polizaRepository;
    this.coreNotifier = coreNotifier;
    this.ipcRate = ipcRate;
  }

  @Transactional
  public PolizaResponse crear(PolizaRequest request) {
    Poliza poliza = new Poliza(request.tipo(), request.vigenciaMeses(), request.canonMensual());
    request.riesgos().stream()
        .map(riesgoRequest -> new Riesgo(riesgoRequest.descripcion()))
        .forEach(poliza::agregarRiesgo);
    return PolizaResponse.from(polizaRepository.save(poliza));
  }

  @Transactional(readOnly = true)
  public List<PolizaResponse> listar(TipoPoliza tipo, EstadoPoliza estado) {
    List<Poliza> polizas;
    if (tipo != null && estado != null) {
      polizas = polizaRepository.findByTipoAndEstado(tipo, estado);
    } else if (tipo != null) {
      polizas = polizaRepository.findByTipo(tipo);
    } else if (estado != null) {
      polizas = polizaRepository.findByEstado(estado);
    } else {
      polizas = polizaRepository.findAll();
    }
    return polizas.stream().map(PolizaResponse::from).toList();
  }

  @Transactional
  public PolizaResponse renovar(Long id) {
    Poliza poliza = obtenerOrThrow(id);
    poliza.renovar(ipcRate);
    PolizaResponse response = PolizaResponse.from(polizaRepository.save(poliza));
    coreNotifier.notificar("RENOVACION", poliza.getId());
    return response;
  }

  @Transactional
  public PolizaResponse cancelar(Long id) {
    Poliza poliza = obtenerOrThrow(id);
    poliza.cancelar();
    PolizaResponse response = PolizaResponse.from(polizaRepository.save(poliza));
    coreNotifier.notificar("CANCELACION", poliza.getId());
    return response;
  }

  Poliza obtenerOrThrow(Long id) {
    return polizaRepository
        .findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Póliza no encontrada"));
  }
}
