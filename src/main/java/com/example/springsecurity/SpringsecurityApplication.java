package com.example.springsecurity;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.web.reactive.config.ResourceHandlerRegistration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.spring6.SpringWebFluxTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.templateresource.SpringResourceTemplateResource;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;

import com.example.springsecurity.app.AppProperties;
import com.example.springsecurity.app.AppProperties.StaticResourceDescription;
import com.example.springsecurity.app.ClasspathAssetsService;
import com.example.springsecurity.app.ModifiableTemplateResource;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
@ConfigurationPropertiesScan(value = {})
@Import({})
@EnableWebFluxSecurity
@EnableCaching
@EnableReactiveMethodSecurity(useAuthorizationManager = true)
public class SpringsecurityApplication implements WebFluxConfigurer {

	public static void main(String[] args) {
		SpringApplication.run(SpringsecurityApplication.class, args);
	}

	@Autowired
	private ApplicationContext applicationContext;

	@Bean
	SpringWebFluxTemplateEngine templateEngine(ThymeleafProperties properties,
			ObjectProvider<ITemplateResolver> templateResolvers, ObjectProvider<IDialect> dialects) {
		SpringWebFluxTemplateEngine engine = new SpringWebFluxTemplateEngine();
		engine.setEnableSpringELCompiler(properties.isEnableSpringElCompiler());
		engine
				.setRenderHiddenMarkersBeforeCheckboxes(properties.isRenderHiddenMarkersBeforeCheckboxes());
		templateResolvers.orderedStream().forEach(engine::addTemplateResolver);
		dialects.orderedStream().forEach(engine::addDialect);
		return engine;
	}

	@Bean
	SpringResourceTemplateResolver myTemplateResolver(ApplicationContext applicationContext,
			ClasspathAssetsService classpathAssetsService,
			ThymeleafProperties properties) {
		SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver() {

			@Override
			protected ITemplateResource computeTemplateResource(
					final IEngineConfiguration configuration, final String ownerTemplate,
					final String template, final String resourceName, final String characterEncoding,
					final Map<String, Object> templateResolutionAttributes) {
				Resource resource = applicationContext.getResource(resourceName);
				resource = new ModifiableTemplateResource(classpathAssetsService, resource, resourceName);
				return new SpringResourceTemplateResource(resource,
						characterEncoding);
			}

		};
		resolver.setApplicationContext(this.applicationContext);
		resolver.setPrefix(properties.getPrefix());
		resolver.setSuffix(properties.getSuffix());
		resolver.setTemplateMode(properties.getMode());
		if (properties.getEncoding() != null) {
			resolver.setCharacterEncoding(properties.getEncoding().name());
		}
		resolver.setCacheable(properties.isCache());
		resolver.setOrder(1);
		resolver.setCheckExistence(properties.isCheckTemplate());
		return resolver;
	}

	/**
	 * @see <a href=
	 *      "https://www.gatsbyjs.com/docs/how-to/previews-deploys-hosting/caching/">Gatsby
	 *      Cache.</a>
	 * @see <a href=
	 *      "https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-caching">Webflux
	 *      cache.</a>
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		AppProperties appProperties = applicationContext.getBean(AppProperties.class);
		log.info(appProperties.serverRootUri());

		for (StaticResourceDescription srd : appProperties.staticResources()) {
			String s = String.join(",", srd.paths());
			log.info("static resource path: {}", s);
			ResourceHandlerRegistration rhr = registry.addResourceHandler(srd.paths())
					.addResourceLocations(srd.locations());
			switch (srd.cache()) {
				case "forever":
					rhr.setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable());
					break;
				case "no":
					rhr.setCacheControl(CacheControl.noCache().cachePublic().mustRevalidate());
					break;
				default:
					break;
			}
		}
	}

}