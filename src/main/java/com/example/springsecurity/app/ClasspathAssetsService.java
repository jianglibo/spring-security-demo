package com.example.springsecurity.app;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import jakarta.annotation.PostConstruct;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ClasspathAssetsService {

  @Autowired
  ResourceLoader resourceLoader;

  private LoadingCache<String, Mono<byte[]>> graphs;

  private Map<String, Bundle> bundlesmap = new HashMap<>();

  public Mono<byte[]> bundleContent(String bundleName) {
    return graphs.get(bundleName);
  }

  @PostConstruct
  void post() throws IOException {

    graphs = Caffeine.newBuilder()
        .maximumSize(10)
        .refreshAfterWrite(30, TimeUnit.DAYS)
        .build(key -> {
          return Mono.fromCallable(() -> {
            Bundle bundle = bundlesmap.get(key);
            if (null == bundle) {
              return null;
            }
            byte[] content = null;
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
              for (String file : bundle.getFiles()) {
                // strip the start slash and preappend "classpath:" to file string:
                // /htmx/assets/js/color-modes.js.
                file = "classpath:" + file.substring(1);
                Resource resource = resourceLoader.getResource(file);
                try (InputStream inputStream = resource.getInputStream()) {
                  byte[] buffer = new byte[1024];
                  int bytesRead;
                  while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                  }
                  outputStream.write("\r\n".getBytes());
                } catch (IOException e) {
                  throw new RuntimeException("fetch " + key + " failed.", e);
                }
              }
              content = outputStream.toByteArray();
            }
            return content;
          }).cache();
        });
  }

  public Bundle uniqueBundle(List<String> urlPaths, boolean js) {
    String uuid = UUID.randomUUID().toString().replace("-", "").toLowerCase();
    Bundle bundle = Bundle.builder().dynamicName(uuid).files(urlPaths).js(js).build();
    if (bundlesmap.containsValue(bundle)) {
      bundlesmap.entrySet().removeIf(entry -> entry.getValue().equals(bundle));
    }
    bundlesmap.put(uuid, bundle);
    return bundle;
  }

  public Path path(String bundleName) {
    return Path.of("");
  }

  @EqualsAndHashCode(exclude = {"dynamicName"})
  @Builder
  public static class Bundle {
    private String templateResourceName;
    @Getter
    private List<String> files;
    @Setter
    private String dynamicName;
    private boolean js;

    public String getUrl() {
      return "/dynamic-forever/" + dynamicName + (js ? ".js" : ".css");
    }
  }

}
