package com.example.springsecurity.htmx;

import lombok.Getter;

public enum HxSwap {

  // innerHTML, outerHTML, beforebegin, afterbegin, beforeend, afterend, delete, none;
  // convert this to have value property and the enum name to upcase.
  INNER_HTML("innerHTML"), OUTER_HTML("outerHTML"), BEFORE_BEGIN("beforebegin"), AFTER_BEGIN(
      "afterbegin"), BEFORE_END("beforeend"), AFTER_END("afterend"), DELETE("delete"), NONE("none");

  @Getter
  private String value;

  private HxSwap(String value) {
    this.value = value;
  }

}
