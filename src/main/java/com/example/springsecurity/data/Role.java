package com.example.springsecurity.data;

import org.springframework.security.core.GrantedAuthority;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Role implements GrantedAuthority {

  private Long id;
  private String name;

  @Override
  public String getAuthority() {
    return name;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof Role) {
      return this.name.equals(((Role) obj).name);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return this.name.hashCode();
  }
}
