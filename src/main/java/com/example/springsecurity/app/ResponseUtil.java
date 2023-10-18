package com.example.springsecurity.app;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.function.server.ServerResponse.BodyBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import reactor.core.publisher.Mono;

public class ResponseUtil {

  public static BodyBuilder found(URI location) {
    return ServerResponse.status(HttpStatus.FOUND)
        .location(location);
  }

  public static BodyBuilder okJson() {
    return ServerResponse.ok()
        .headers(hds -> hds.setContentType(
            MediaType.APPLICATION_JSON));
  }

  public static Mono<ServerResponse> okHtmlEmpty() {
    return ServerResponse.ok()
        .headers(hds -> hds.setContentType(
            MediaType.TEXT_HTML))
        .bodyValue("");
  }

  public static BodyBuilder okHtml() {
    return ServerResponse.ok()
        .headers(hds -> hds.setContentType(
            MediaType.TEXT_HTML));
  }

  public static BodyBuilder okText() {
    return ServerResponse.ok()
        .headers(hds -> hds.setContentType(
            MediaType.TEXT_PLAIN));
  }

  public static <T> Mono<ServerResponse> okJsonDataList(List<T> list) {
    return ServerResponse.ok()
        .headers(hds -> hds.setContentType(
            MediaType.APPLICATION_JSON))
        .bodyValue(Map.of("data", list));
  }

  public static Mono<ServerResponse> okJsonDataObject(Object obj) {
    return ServerResponse.ok()
        .headers(hds -> hds.setContentType(
            MediaType.APPLICATION_JSON))
        .bodyValue(Map.of("data", obj));
  }

  public static Mono<ServerResponse> okJsonMessage(String message) {
    return ServerResponse.ok()
        .headers(hds -> hds.setContentType(
            MediaType.APPLICATION_JSON))
        .bodyValue(CommonResponseBody.messageBody(message));
  }

  public static Mono<ServerResponse> okJsonErrorMessage(String... messages) {
    return ServerResponse.ok()
        .headers(hds -> hds.setContentType(
            MediaType.APPLICATION_JSON))
        .bodyValue(CommonResponseBody.errorMessageBody(messages));
  }

  public static Mono<ServerResponse> foundToInfoPage(String info) {
    UriComponents uri = UriComponentsBuilder.fromPath("/info/")
        .queryParam("content", info).build();
    return found(uri.toUri()).build();
  }

  public static Mono<ServerResponse> badRequest() {
    return ServerResponse.badRequest().bodyValue("not allowed.");
  }

  public static Mono<ServerResponse> badRequest(String message) {
    return ServerResponse.badRequest().bodyValue(message);
  }

  public static Mono<ServerResponse> asyncOk() {
    return okJson().bodyValue(Map.of("async", true));
  }


  @JsonInclude(Include.NON_NULL)
  @Data
  public static class CommonResponseBody {
    private Map<String, Object> data;
    private List<Map<String, Object>> errors;

    public static CommonResponseBody messageBody(String message) {
      CommonResponseBody cr = new CommonResponseBody();
      cr.setData(Map.of("message", message));
      return cr;
    }

    public static Object errorMessageBody(String... messages) {
      CommonResponseBody cr = new CommonResponseBody();
      cr.setErrors(Arrays.stream(messages).map(s -> Map.of("message", (Object) s)).toList());
      return cr;
    }
  }
}

