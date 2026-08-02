package com.crubio.polizas.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiKeyFilterIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void rechazaRequestSinApiKey() throws Exception {
    mockMvc.perform(get("/polizas")).andExpect(status().isUnauthorized());
  }

  @Test
  void rechazaRequestConApiKeyInvalida() throws Exception {
    mockMvc
        .perform(get("/polizas").header("x-api-key", "clave-incorrecta"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void aceptaRequestConApiKeyValida() throws Exception {
    mockMvc.perform(get("/polizas").header("x-api-key", "123456")).andExpect(status().isOk());
  }
}
