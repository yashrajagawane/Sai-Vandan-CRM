package com.saivandan.crm.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
  @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
    String id = request.getHeader("X-Correlation-Id");
    if (id == null || id.isBlank()) id = UUID.randomUUID().toString();
    response.setHeader("X-Correlation-Id", id);
    chain.doFilter(request, response);
  }
}
