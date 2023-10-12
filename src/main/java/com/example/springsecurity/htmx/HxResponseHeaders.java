package com.example.springsecurity.htmx;

import lombok.Getter;

public enum HxResponseHeaders {


  TRIGGER("HX-Trigger"), TRIGGER_AFTER_SETTLE("HX-Trigger-After-Settle"), TRIGGER_AFTER_SWAP(
      "HX-Trigger-After-Swap"), RESWAP("HX-Reswap"), RESELECT("HX-Reselect"), RETARGET(
          "HX-Retarget"), REFRESH("HX-Refresh"), REPLACE_URL("HX-Replace-Url"), REDIRECT(
              "HX-Redirect"), PUSH_URL("HX-Push-Url"), LOCATION("HX-Location");

  @Getter
  private String value;

  private HxResponseHeaders(String value) {
    this.value = value;
  }



}
