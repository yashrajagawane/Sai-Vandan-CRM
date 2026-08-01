package com.saivandan.crm.security;

import com.saivandan.crm.user.AppUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwt; private final AppUserRepository users;
  public JwtAuthenticationFilter(JwtService jwt, AppUserRepository users) { this.jwt = jwt; this.users = users; }
  @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith("Bearer ")) {
      try {
        String raw = header.substring(7);
        if (!"access".equals(jwt.tokenType(raw))) throw new IllegalArgumentException("Not an access token");
        String email = jwt.subject(raw);
        users.findByEmailIgnoreCase(email).filter(u -> u.isActive()).ifPresent(user -> {
          CurrentUser principal = new CurrentUser(user);
          SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        });
      } catch (Exception ignored) { SecurityContextHolder.clearContext(); }
    }
    chain.doFilter(request, response);
  }
}
