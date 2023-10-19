package com.example.springsecurity.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebFilter;

import com.example.springsecurity.data.UserRepo;
import com.example.springsecurity.htmx.HxResponseUtil;
import com.example.springsecurity.htmx.ThymeleafCtxFactory;

import reactor.core.publisher.Mono;

@Component
public class HtmxwebService {

  @Autowired
  HxResponseUtil hxResponseUtil;

  @Autowired
  ServerRequestCache webSessionServerRequestCache;

  @Autowired
  ApplicationContext applicationContext;

  @Autowired
  SecurityWebFilterChain securityWebFilterChain;

  @Autowired
  Environment environment;

  @Autowired
  UserRepo userRepo;

  @Autowired
  ThymeleafCtxFactory thymeleafCtxFactory;

  public Mono<ServerResponse> index(ServerRequest req) {
    return webSessionServerRequestCache.getRedirectUri(req.exchange())
        .map(uri -> uri.toString())
        .defaultIfEmpty("No saved request, you are visiting this page directly.")
        .flatMap(savedRequest -> {
          Map<String, Object> model = Map.of("savedRequest", savedRequest, "users", userRepo.getUsers());
          return thymeleafCtxFactory.create(req, model).flatMap(ctx -> {
            return ServerResponse.ok().render("index.html", ctx.getModel());
          });
        });
  }

  public Mono<ServerResponse> listWebfilterBeans(ServerRequest req) {
    Mono<CsrfToken> csrfToken = req.exchange().getRequiredAttribute(CsrfToken.class.getName());
    return csrfToken
        .flatMap(csrf -> {

          Map<String, WebFilter> webFilterBeans = applicationContext.getBeansOfType(WebFilter.class);
          // Sort the WebFilters based on their order
          List<WebFilter> sortedWebFilters = new ArrayList<>(webFilterBeans.values());
          sortedWebFilters.sort(AnnotationAwareOrderComparator.INSTANCE);
          return securityWebFilterChain.getWebFilters().map(c -> c.getClass().getName())
              .collectList().flatMap(securityFilters -> {
                Map<String, Object> model = Map.of(
                    "default_locale", Locale.getDefault(),
                    "sortedWebFilters",
                    Stream
                        .of(sortedWebFilters.stream().map(c -> c.getClass().getName()), Stream.of("--separator---"),
                            securityFilters.stream())
                        .flatMap(c -> c).collect(Collectors.toList()),
                    "fileEncode", System.getProperty("file.encoding"));
                return thymeleafCtxFactory.create(req, model).flatMap(thymeleafCtx -> {
                  return ServerResponse.ok().render("filters", thymeleafCtx.getModel());
                });
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
                return thymeleafCtxFactory.create(req, model).flatMap(ctx -> {
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
          return thymeleafCtxFactory.create(req, model) //
              .flatMap(ctx -> {
                return ServerResponse.ok().render("login.html", ctx.getModel());
              });
        });
  }

  public Mono<ServerResponse> protecteda(ServerRequest req) {
    return thymeleafCtxFactory.create(req, null) //
        .flatMap(ctx -> {
          if (req.headers().header(HttpHeaders.ACCEPT).contains("application/json")) {
            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON) //
                .bodyValue(ctx.getModel().get("user"));
          }
          return ServerResponse.ok().render("app/protecteda.html", ctx.getModel());
        });

  }

  public Mono<ServerResponse> protectedadmin(ServerRequest req) {
    return thymeleafCtxFactory.create(req, null) //
        .flatMap(ctx -> {
          return ServerResponse.ok().render("app/protectedadmina.html", ctx.getModel());
        });

  }

  public Mono<ServerResponse> accessdenedpage(ServerRequest req) {
    return thymeleafCtxFactory.create(req, null) //
        .flatMap(ctx -> {
          return ServerResponse.ok().render("app/custom-access-deny-page", ctx.getModel());
        });

  }

  public Mono<ServerResponse> tpl(ServerRequest req, String tpl) {
    Map<String, String> queryMap = req.queryParams().toSingleValueMap();
    Map<String, Object> model = new HashMap<>(queryMap);
    return thymeleafCtxFactory.create(req, model) //
        .flatMap(ctx -> {
          return ServerResponse.ok().render(tpl, ctx.getModel());
        });
  }

}
