package com.example.springsecurity.app;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.springsecurity.data.UserRepo;
import com.example.springsecurity.htmx.HxResponseUtil;
import com.example.springsecurity.htmx.ThymeleafCtx;

import reactor.core.publisher.Mono;

@Component
public class HtmxwebService {

  @Autowired
  HxResponseUtil hxResponseUtil;

  @Autowired
  ServerRequestCache webSessionServerRequestCache;

  @Autowired
  UserRepo userRepo;

  public Mono<ServerResponse> index(ServerRequest req) {
    return webSessionServerRequestCache.getRedirectUri(req.exchange())
        .map(uri -> uri.toString())
        .defaultIfEmpty("No saved request, you are visiting this page directly.")
        .flatMap(savedRequest -> {
          Map<String, Object> model = Map.of("savedRequest", savedRequest, "users", userRepo.getUsers());
          return ThymeleafCtx.create(req, model).flatMap(ctx -> {
            return ServerResponse.ok().render("index.html", ctx.getModel());
          });
        });
  }

  public Mono<ServerResponse> loginGet(ServerRequest req) {
    return webSessionServerRequestCache.getRedirectUri(req.exchange())
        .map(uri -> uri.toString())
        .defaultIfEmpty("No saved request, you are visiting this page directly.")
        .flatMap(savedRequest -> {
          return req.exchange().<Mono<CsrfToken>>getAttribute(CsrfToken.class.getName()).map(o -> o.getToken())
              .defaultIfEmpty("no csrf present.")
              .flatMap(ck -> {
                Map<String, Object> model = Map.of("savedRequest", savedRequest,
                    "users", userRepo.getUsers());
                return ThymeleafCtx.create(req, model).flatMap(ctx -> {
                  return ServerResponse.ok().render("login.html", ctx.getModel());
                });
              });
        });
  }

  public Mono<ServerResponse> loginPost(ServerRequest req) {
    return webSessionServerRequestCache.getRedirectUri(req.exchange())
        .map(uri -> uri.toString())
        .defaultIfEmpty("No saved request, you are visiting this page directly.")
        .flatMap(savedRequest -> {
          Map<String, Object> model = Map.of("savedRequest", savedRequest);
          return ThymeleafCtx.create(req, model) //
              .flatMap(ctx -> {
                return ServerResponse.ok().render("login.html", ctx.getModel());
              });
        });
  }

  public Mono<ServerResponse> protecteda(ServerRequest req) {
    return ThymeleafCtx.create(req, null) //
        .flatMap(ctx -> {
          if (req.headers().header(HttpHeaders.ACCEPT).contains("application/json")) {
            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON) //
                .bodyValue(ctx.getModel().get("user"));
          }
          return ServerResponse.ok().render("app/protecteda.html", ctx.getModel());
        });

  }

  public Mono<ServerResponse> protectedadmin(ServerRequest req) {
    return ThymeleafCtx.create(req, null) //
        .flatMap(ctx -> {
          return ServerResponse.ok().render("app/protectedadmina.html", ctx.getModel());
        });

  }

  public Mono<ServerResponse> accessdenedpage(ServerRequest req) {
    return ThymeleafCtx.create(req, null) //
        .flatMap(ctx -> {
          return ServerResponse.ok().render("app/custom-access-deny-page", ctx.getModel());
        });

  }

  public Mono<ServerResponse> tpl(ServerRequest req, String tpl) {
    Map<String, String> queryMap = req.queryParams().toSingleValueMap();
    Map<String, Object> model = new HashMap<>(queryMap);
    return ThymeleafCtx.create(req, model) //
        .flatMap(ctx -> {
          return ServerResponse.ok().render(tpl, ctx.getModel());
        });
  }

}
