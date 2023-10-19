package com.example.springsecurity.app;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import com.example.springsecurity.app.AppProperties.EasyAuthProperties;
import com.example.springsecurity.app.AppProperties.PlaygroundSettings;
import com.example.springsecurity.app.AppProperties.StaticResourceDescription;

@ConfigurationProperties("app")
public record AppProperties(
    EasyAuthProperties easyAuth,
    String serverRootUri,
    List<StaticResourceDescription> staticResources,
    PlaygroundSettings playground) {

  public StaticResourceDescription gatsbyPublic() {
    return staticResources().stream().filter(sr -> "gatsbyPublic".equals(sr.name())).findAny()
        .get();
  }

  public static record EasyAuthProperties(String protectedArea, String loginPage) {
  }

  public static record StaticResourceDescription(String[] paths, String[] locations, String cache,
      Set<String> excludes, String name) {
  }

  public static record SpecialFileResources(Resource indexHtml, Resource loginHtml) {
  }

  public static record PlaygroundSettings(Path baseDir, String instanceId, Path defaultAppPath,
      List<String> watchIncludes,
      String tellRefreshEndpoint,
      List<String> watchExcludes, int userCount) {
  }

}
