package com.example.springsecurity.app;

import static org.springframework.web.reactive.function.server.RequestPredicates.all;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties.Resources;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.HtmlUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/*
 * Copypaster from the @see DefaultErrorWebExceptionHandler
 */
public class DefaultErrorWebExceptionHandlerModified extends AbstractErrorWebExceptionHandler {

	private static final MediaType TEXT_HTML_UTF8 = new MediaType("text", "html", StandardCharsets.UTF_8);

	private static final Map<HttpStatus.Series, String> SERIES_VIEWS;

	static {
		Map<HttpStatus.Series, String> views = new EnumMap<>(HttpStatus.Series.class);
		views.put(HttpStatus.Series.CLIENT_ERROR, "4xx");
		views.put(HttpStatus.Series.SERVER_ERROR, "5xx");
		SERIES_VIEWS = Collections.unmodifiableMap(views);
	}

	private final ErrorProperties errorProperties;

	/**
	 * Create a new {@code DefaultErrorWebExceptionHandler} instance.
	 * 
	 * @param errorAttributes    the error attributes
	 * @param resources          the resources configuration properties
	 * @param errorProperties    the error configuration properties
	 * @param applicationContext the current application context
	 * @since 2.4.0
	 */
	public DefaultErrorWebExceptionHandlerModified(ErrorAttributes errorAttributes, Resources resources,
			ErrorProperties errorProperties, ApplicationContext applicationContext) {
		super(errorAttributes, resources, applicationContext);
		this.errorProperties = errorProperties;
	}

	@Override
	protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
		return route(acceptsTextHtml(), this::renderErrorView).andRoute(all(), this::renderErrorResponse);
	}

	/**
	 * Render the error information as an HTML view.
	 * 
	 * @param request the current request
	 * @return a {@code Publisher} of the HTTP response
	 */
	protected Mono<ServerResponse> renderErrorView(ServerRequest request) {
		Map<String, Object> error = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.TEXT_HTML));
		int errorStatus = getHttpStatus(error);
		ServerResponse.BodyBuilder responseBody = ServerResponse.status(errorStatus).contentType(TEXT_HTML_UTF8);
		return Flux.just(getData(errorStatus).toArray(new String[] {}))
				.flatMap((viewName) -> renderErrorView(viewName, responseBody, error))
				.switchIfEmpty(this.errorProperties.getWhitelabel().isEnabled()
						? renderDefaultErrorView(responseBody, error)
						: Mono.error(getError(request)))
				.next();
	}

	private List<String> getData(int errorStatus) {
		List<String> data = new ArrayList<>();
		data.add("error/" + errorStatus);
		HttpStatus.Series series = HttpStatus.Series.resolve(errorStatus);
		if (series != null) {
			data.add("error/" + SERIES_VIEWS.get(series));
		}
		data.add("error/error");
		return data;
	}

	/**
	 * Render the error information as a JSON payload.
	 * 
	 * @param request the current request
	 * @return a {@code Publisher} of the HTTP response
	 */
	protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
		Map<String, Object> error = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
		return ServerResponse.status(getHttpStatus(error))
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(error));
	}

	protected ErrorAttributeOptions getErrorAttributeOptions(ServerRequest request, MediaType mediaType) {
		ErrorAttributeOptions options = ErrorAttributeOptions.defaults();
		if (this.errorProperties.isIncludeException()) {
			options = options.including(Include.EXCEPTION);
		}
		if (isIncludeStackTrace(request, mediaType)) {
			options = options.including(Include.STACK_TRACE);
		}
		if (isIncludeMessage(request, mediaType)) {
			options = options.including(Include.MESSAGE);
		}
		if (isIncludeBindingErrors(request, mediaType)) {
			options = options.including(Include.BINDING_ERRORS);
		}
		return options;
	}

	/**
	 * Determine if the stacktrace attribute should be included.
	 * 
	 * @param request  the source request
	 * @param produces the media type produced (or {@code MediaType.ALL})
	 * @return if the stacktrace attribute should be included
	 */
	protected boolean isIncludeStackTrace(ServerRequest request, MediaType produces) {
		return switch (this.errorProperties.getIncludeStacktrace()) {
			case ALWAYS -> true;
			case ON_PARAM -> isTraceEnabled(request);
			default -> false;
		};
	}

	/**
	 * Determine if the message attribute should be included.
	 * 
	 * @param request  the source request
	 * @param produces the media type produced (or {@code MediaType.ALL})
	 * @return if the message attribute should be included
	 */
	protected boolean isIncludeMessage(ServerRequest request, MediaType produces) {
		return switch (this.errorProperties.getIncludeMessage()) {
			case ALWAYS -> true;
			case ON_PARAM -> isMessageEnabled(request);
			default -> false;
		};
	}

	/**
	 * Determine if the errors attribute should be included.
	 * 
	 * @param request  the source request
	 * @param produces the media type produced (or {@code MediaType.ALL})
	 * @return if the errors attribute should be included
	 */
	protected boolean isIncludeBindingErrors(ServerRequest request, MediaType produces) {
		return switch (this.errorProperties.getIncludeBindingErrors()) {
			case ALWAYS -> true;
			case ON_PARAM -> isBindingErrorsEnabled(request);
			default -> false;
		};
	}

	/**
	 * Get the HTTP error status information from the error map.
	 * 
	 * @param errorAttributes the current error information
	 * @return the error HTTP status
	 */
	protected int getHttpStatus(Map<String, Object> errorAttributes) {
		return (int) errorAttributes.get("status");
	}

	/**
	 * Predicate that checks whether the current request explicitly support
	 * {@code "text/html"} media type.
	 * <p>
	 * The "match-all" media type is not considered here.
	 * 
	 * @return the request predicate
	 */
	protected RequestPredicate acceptsTextHtml() {
		return (serverRequest) -> {
			try {
				List<MediaType> acceptedMediaTypes = serverRequest.headers().accept();
				acceptedMediaTypes.removeIf(MediaType.ALL::equalsTypeAndSubtype);
				MimeTypeUtils.sortBySpecificity(acceptedMediaTypes);
				return acceptedMediaTypes.stream().anyMatch(MediaType.TEXT_HTML::isCompatibleWith);
			} catch (InvalidMediaTypeException ex) {
				return false;
			}
		};
	}

	protected Mono<ServerResponse> renderDefaultErrorView(ServerResponse.BodyBuilder responseBody,
			Map<String, Object> error) {
		StringBuilder builder = new StringBuilder();
		Date timestamp = (Date) error.get("timestamp");
		Object message = error.get("message");
		Object trace = error.get("trace");
		Object requestId = error.get("requestId");
		builder.append("<html><body><h1>Whitelabel Error Page</h1>")
				.append("<p>This application has no configured error view, so you are seeing this as a fallback.</p>")
				.append("<div id='created'>")
				.append(timestamp)
				.append("</div>")
				.append("<div style=\"color:red;\">[")
				.append(requestId)
				.append("] There was an unexpected error (type=")
				.append(htmlEscape(error.get("error")))
				.append(", status=")
				.append(htmlEscape(error.get("status")))
				.append(").</div>");
		if (message != null) {
			builder.append("<div><b>").append(htmlEscape(message)).append("</b></div>");
		}
		if (trace != null) {
			builder.append("<div style='white-space:pre-wrap;'>").append(deco(trace)).append("</div>");
		}
		builder.append("</body></html>");
		return responseBody.bodyValue(builder.toString());
	}

	private Object deco(Object trace) {
		String str = trace.toString();
		return Stream.of(str.split("\n")).map(s -> {
			if (s.contains("com.example") || s.contains("Caused by:")) {
				return "<span style='color:red'>" + htmlEscape(s) + "</span>";
			}
			return "<span>" + htmlEscape(s) + "</span>";
		}).reduce("",
				(a, b) -> a + "\n" + b);
	}

	private String htmlEscape(Object input) {
		return (input != null) ? HtmlUtils.htmlEscape(input.toString()) : null;
	}

}

