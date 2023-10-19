package com.example.springsecurity.htmx;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;

public enum HxResponseHeaders {

  TRIGGER("HX-Trigger"), TRIGGER_AFTER_SETTLE("HX-Trigger-After-Settle"), TRIGGER_AFTER_SWAP(
      "HX-Trigger-After-Swap"),
  RESWAP("HX-Reswap"), RESELECT("HX-Reselect"), RETARGET(
      "HX-Retarget"),
  REFRESH("HX-Refresh"), REPLACE_URL("HX-Replace-Url"), REDIRECT(
      "HX-Redirect"),
  PUSH_URL("HX-Push-Url"), LOCATION("HX-Location");

  @Getter
  private String value;

  private HxResponseHeaders(String value) {
    this.value = value;
  }

  public static HxResponseHeaders fromValue(String value) {
    for (HxResponseHeaders v : HxResponseHeaders.values()) {
      if (v.getValue().equalsIgnoreCase(value)) {
        return v;
      }
    }
    return null;
  }

  public static List<Map<String, String>> toList() {
    return Arrays.stream(HxResponseHeaders.values()).map(v -> {
      return Map.of("name", v.getValue(), "value", v.getValue());
    })
        .map(v -> {
          if (v.get("name").equals(HxResponseHeaders.TRIGGER.getValue())) {
            Map<String, String> nv = new HashMap<>(v);
            nv.put("defaultValue", "{\"startAvailableCheck\":{}}");
            return nv;
          }
          return v;
        })
        .collect(Collectors.toList());
  }

}
