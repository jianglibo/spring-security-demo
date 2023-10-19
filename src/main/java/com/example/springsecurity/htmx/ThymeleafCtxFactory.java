package com.example.springsecurity.htmx;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import com.example.springsecurity.app.AppProperties;
import com.example.springsecurity.data.User;
import com.example.springsecurity.tool.MyLangUtil;

import lombok.Getter;
import reactor.core.publisher.Mono;

@Component
public class ThymeleafCtxFactory {

	@Autowired
	AppProperties appProperties;

	public Mono<ThymeleafCtx> createRequireAppInstance(ServerRequest req, Map<String, Object> model) {
		return create(req, model, true);
	}

	public Mono<ThymeleafCtx> create(ServerRequest req, Map<String, Object> model) {
		return create(req, model, false);
	}

	public Mono<ThymeleafCtx> create(ServerRequest req, Map<String, Object> model, boolean requireInstance) {
		return ReactiveSecurityContextHolder.getContext()
				.flatMap(ctx -> Mono.justOrEmpty(ctx.getAuthentication()))
				.map(auth -> auth.getPrincipal()).cast(User.class)
				.map(user -> {
					return Map.of("user", user);
				}).defaultIfEmpty(Map.of())
				.map(m -> {
					Map<String, Object> mm = new HashMap<>();
					if (model != null) {
						mm.putAll(model);
					}
					mm.putAll(m);
					mm.put("appProperties", appProperties);
					return new ThymeleafCtx(req, mm);
				});
	}

	@Getter
	public static class ThymeleafCtx {
		private final ServerRequest req;
		private final Map<String, Object> model;

		/**
		 * @param req
		 */
		private ThymeleafCtx(ServerRequest req, Map<String, Object> model) {
			this.req = req;
			this.model = model != null ? model : Collections.emptyMap();
		}

		public boolean localeIs(String locale) {
			LocaleContext localeContext = req.exchange().getLocaleContext();
			if (localeContext == null) {
				return false;
			}
			Locale lc = localeContext.getLocale();
			return lc != null && lc.getLanguage().equals(locale);
		}

		public String currentLanguage() {
			LocaleContext localeContext = req.exchange().getLocaleContext();
			if (localeContext == null) {
				return "English";
			}
			Locale lc = localeContext.getLocale();
			if (lc == null) {
				return "English";
			}
			String lang = lc.getLanguage();
			if ("zh".equals(lang)) {
				return "中文";
			} else if ("ja".equals(lang)) {
				return "日本語";
			} else {
				return "English";
			}
		}

		public List<String[]> allLanguages() {
			return List.of(new String[] { "English", "en" }, new String[] { "中文", "zh" }, new String[] { "日本語", "ja" });
		}

		public boolean pathIs(String path) {
			String reqPath = req.path();
			return reqPath.equals(path);
		}

		public boolean pathStartsWith(String path) {
			String reqPath = req.path();
			return reqPath.startsWith(path);
		}

		public boolean pathMatch(String ptn) {
			String reqPath = req.path();
			boolean b = reqPath.matches(ptn);
			return b;
		}

		public String currentPath() {
			return req.path();
		}

		public String currentUrl() {
			if (MyLangUtil.isBlank(req.uri().getRawQuery())) {
				return req.uri().getRawPath();
			}
			return req.uri().getRawPath() + "?" + req.uri().getRawQuery();
		}

		public String parentPath() {
			String reqPath = req.path();
			int idx = reqPath.lastIndexOf('/');
			if (idx > 0) {
				return reqPath.substring(0, idx);
			} else {
				return reqPath;
			}
		}

		public String calSortby(String field) {
			String inParam = req.queryParam("sortby").orElse("");
			if (field.equals(inParam)) {
				return "-" + field;
			} else if (("-" + field).equals(inParam)) {
				return field;
			} else {
				return field;
			}
		}

		public String param(String name, String defaultValue) {
			return req.queryParam(name).orElse(defaultValue);
		}

		public String pathVariable(String name) {
			return req.pathVariable(name);
		}

		public Map<String, Object> getModel() {
			return getModel(Map.of());
		}

		// merge the model from the request and the model from the ctx
		public Map<String, Object> getModel(Map<String, Object> extra) {
			Map<String, Object> map = new HashMap<>();
			map.put("req", req);
			map.put("tctx", this);
			map.putAll(model);
			String uuid = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
			String duuid = "d" + uuid;
			map.put("duuid", duuid);
			map.put("fuuid", "f" + uuid);
			map.put("fileEncode", System.getProperty("file.encoding"));
			map.put("default_locale", Locale.getDefault());
			if (HxRequestHeaders.isHxRequest(req)) {
				String xt = req.headers().firstHeader(HxRequestHeaders.TARGET.getValue());
				if (xt == null) {
					xt = duuid;
				}
				map.put("cid", xt);
			}
			if (extra != null) {
				map.putAll(extra);
			}
			return Collections.unmodifiableMap(map);
		}

	}

}
