package com.crubio.polizas.poliza;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolizaRepository extends JpaRepository<Poliza, Long> {

  List<Poliza> findByTipo(TipoPoliza tipo);

  List<Poliza> findByEstado(EstadoPoliza estado);

  List<Poliza> findByTipoAndEstado(TipoPoliza tipo, EstadoPoliza estado);
}
