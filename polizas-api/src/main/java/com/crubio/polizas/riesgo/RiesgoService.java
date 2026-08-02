package com.crubio.polizas.riesgo;

import com.crubio.polizas.common.BusinessRuleException;
import com.crubio.polizas.core.CoreNotifier;
import com.crubio.polizas.poliza.Poliza;
import com.crubio.polizas.poliza.PolizaRepository;
import com.crubio.polizas.poliza.TipoPoliza;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RiesgoService {

  private final PolizaRepository polizaRepository;
  private final RiesgoRepository riesgoRepository;
  private final CoreNotifier coreNotifier;

  public RiesgoService(
      PolizaRepository polizaRepository,
      RiesgoRepository riesgoRepository,
      CoreNotifier coreNotifier) {
    this.polizaRepository = polizaRepository;
    this.riesgoRepository = riesgoRepository;
    this.coreNotifier = coreNotifier;
  }

  @Transactional(readOnly = true)
  public List<RiesgoResponse> listarPorPoliza(Long polizaId) {
    return obtenerPolizaOrThrow(polizaId).getRiesgos().stream().map(RiesgoResponse::from).toList();
  }

  @Transactional
  public RiesgoResponse agregar(Long polizaId, RiesgoRequest request) {
    Poliza poliza = obtenerPolizaOrThrow(polizaId);
    if (poliza.getTipo() != TipoPoliza.COLECTIVA) {
      throw new BusinessRuleException("Solo pólizas colectivas pueden agregar riesgos");
    }
    Riesgo riesgo = new Riesgo(request.descripcion());
    poliza.agregarRiesgo(riesgo);
    Riesgo guardado = riesgoRepository.save(riesgo);
    coreNotifier.notificar("AGREGAR_RIESGO", poliza.getId());
    return RiesgoResponse.from(guardado);
  }

  @Transactional
  public RiesgoResponse cancelar(Long riesgoId) {
    Riesgo riesgo =
        riesgoRepository
            .findById(riesgoId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Riesgo no encontrado"));
    riesgo.cancelar();
    RiesgoResponse response = RiesgoResponse.from(riesgoRepository.save(riesgo));
    coreNotifier.notificar("CANCELAR_RIESGO", riesgo.getPoliza().getId());
    return response;
  }

  private Poliza obtenerPolizaOrThrow(Long polizaId) {
    return polizaRepository
        .findById(polizaId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Póliza no encontrada"));
  }
}
