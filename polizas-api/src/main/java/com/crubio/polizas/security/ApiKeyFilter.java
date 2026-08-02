package com.crubio.polizas.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

  private static final String API_KEY_HEADER = "x-api-key";

  private final String expectedApiKey;

  public ApiKeyFilter(@Value("${polizas.api-key}") String expectedApiKey) {
    this.expectedApiKey = expectedApiKey;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String path = request.getRequestURI();
    if (!path.startsWith("/polizas") && !path.startsWith("/riesgos")) {
      filterChain.doFilter(request, response);
      return;
    }

    String apiKey = request.getHeader(API_KEY_HEADER);
    if (!expectedApiKey.equals(apiKey)) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    filterChain.doFilter(request, response);
  }
}
