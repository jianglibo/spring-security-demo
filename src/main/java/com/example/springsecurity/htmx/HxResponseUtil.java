
package com.example.springsecurity.htmx;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Component
public class HxResponseUtil {

  private static final String SWEET_ALERT_TRIGGER_KEY = "SweetAlert";

  @Autowired
  ObjectMapper objectMapper;

  private String swalHeaderValue(Swal swal) {
    String hbody;
    try {
      hbody = objectMapper
          .writeValueAsString(
              Map.of(SWEET_ALERT_TRIGGER_KEY, objectMapper.writeValueAsString(swal)));
      return hbody;
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  // https://htmx.org/reference/
  // HX-Trigger: {"event1":"A message", "event2":"Another message"}
  public Mono<ServerResponse> sweetalertSuccess(String text) {
    return sendhxmsg(swalHeaderValue(Swal.success("Success", text)));
  }

  public Mono<ServerResponse> sweetalertFailed(String text) {
    return sendhxmsg(swalHeaderValue(Swal.error("Failed", text)));
  }

  private Mono<ServerResponse> sendhxmsg(String hbody) {
    return ServerResponse.noContent()
        .header(HxResponseHeaders.TRIGGER.getValue(), hbody)
        .header(HxResponseHeaders.RESWAP.getValue(), HxSwap.NONE.getValue())
        .build();
  }

  public Mono<ServerResponse> r204() {
    return ServerResponse.noContent().build();
  }

  // HX-Reswap: beforeend
  // HX-Retarget: document
  public Mono<ServerResponse> toast(SwalIcon icon, String message) {
    return ServerResponse.ok().headers(hds -> {
      hds.add(HxResponseHeaders.RESWAP.getValue(), HxSwap.BEFORE_END.getValue());
      hds.add(HxResponseHeaders.RETARGET.getValue(), "body");
    }).render("common/toast :: toast", Map.of("icon", icon.getValue(),
        "message", message));
  }

  public static enum SwalIcon {
    SUCCESS("success"), ERROR("error"), WARNING("warning"), INFO("info"), QUESTION("question");

    private String value;

    private SwalIcon(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  /**
   * https://htmx.org/headers/hx-location/
   * 
   * @param req
   * @param uri
   * @return
   */
  public Mono<ServerResponse> locationTo(ServerRequest req, String uri) {
    String hxTarget = req.headers().firstHeader(HxRequestHeaders.TARGET.getValue());
    Assert.notNull(hxTarget, "hxTarget is null");
    if (!hxTarget.startsWith("#")) {
      hxTarget = "#" + hxTarget;
    }
    String hxlocation;
    try {
      hxlocation = objectMapper.writeValueAsString(Map.of("path", uri, "target", hxTarget));
      return ServerResponse.ok()
          .header(HxResponseHeaders.LOCATION.getValue(), hxlocation)
          .build();
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public Mono<ServerResponse> redirectTo(String uri) {
    return ServerResponse.ok()
        .header(HxResponseHeaders.REDIRECT.getValue(), uri)
        .build();
  }

}
