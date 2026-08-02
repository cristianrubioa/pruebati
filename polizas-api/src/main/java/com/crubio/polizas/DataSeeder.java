package com.crubio.polizas;

import com.crubio.polizas.poliza.Poliza;
import com.crubio.polizas.poliza.PolizaRepository;
import com.crubio.polizas.poliza.TipoPoliza;
import com.crubio.polizas.riesgo.Riesgo;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

  private final PolizaRepository polizaRepository;

  public DataSeeder(PolizaRepository polizaRepository) {
    this.polizaRepository = polizaRepository;
  }

  @Override
  public void run(String... args) {
    Poliza individual = new Poliza(TipoPoliza.INDIVIDUAL, 12, new BigDecimal("1500000"));
    individual.agregarRiesgo(new Riesgo("Incendio - apartamento 402"));
    polizaRepository.save(individual);

    Poliza colectiva = new Poliza(TipoPoliza.COLECTIVA, 12, new BigDecimal("2000000"));
    colectiva.agregarRiesgo(new Riesgo("Incendio - torre A"));
    colectiva.agregarRiesgo(new Riesgo("Robo - torre A"));
    polizaRepository.save(colectiva);
  }
}
