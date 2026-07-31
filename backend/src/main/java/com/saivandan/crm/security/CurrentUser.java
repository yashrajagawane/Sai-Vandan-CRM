package com.saivandan.crm.security;

import com.saivandan.crm.user.AppUser;
import com.saivandan.crm.user.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;

public record CurrentUser(AppUser user) implements UserDetails {
  @Override public Collection<? extends GrantedAuthority> getAuthorities() {
    return user.getRoles().stream().map(Role::getCode).map(role -> new SimpleGrantedAuthority("ROLE_" + role.name())).toList();
  }
  @Override public String getPassword() { return user.getPasswordHash(); }
  @Override public String getUsername() { return user.getEmail(); }
  @Override public boolean isEnabled() { return user.isActive(); }
}

