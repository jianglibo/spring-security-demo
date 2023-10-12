package com.example.springsecurity.htmx;

import java.util.Set;

import jakarta.validation.ConstraintViolation;

public class ValidationErrors<T> {

  private Set<ConstraintViolation<T>> violations;

  public ValidationErrors() {
    this.violations = null;
  }

  /**
   * @param violations
   */
  public ValidationErrors(Set<ConstraintViolation<T>> violations) {
    this.violations = violations;
  }

  public boolean hasErrors() {
    return violations != null && !violations.isEmpty();
  }

  public boolean hasErrors(String field) {
    if (violations == null) {
      return false;
    }
    return violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(field));
  }

  public String getErrorMessage(String field) {
    if (violations == null) {
      return "";
    }
    return violations.stream().filter(v -> v.getPropertyPath().toString().equals(field))
        .map(v -> v.getMessage()).findFirst().orElse(null);
  }
}
