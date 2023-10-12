package com.example.springsecurity.app;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import com.example.springsecurity.app.AppProperties.EasyAuthProperties;
import com.example.springsecurity.app.AppProperties.PlaygroundSettings;
import com.example.springsecurity.app.AppProperties.SpecialFileResources;
import com.example.springsecurity.app.AppProperties.StaticResourceDescription;


@ConfigurationProperties("app")
public record AppProperties(
    EasyAuthProperties easyAuth,
    String serverRootUri,
    List<StaticResourceDescription> staticResources,
    SpecialFileResources specialFileResources,
    PlaygroundSettings playground,
    Path certsTemporaryDir,
    Path uploadDir,
    Path sshkeyDir,
    Path packingDir,
    String appDownloadDir) {

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

  public static record PlaygroundSettings(Path baseDir, Path gradleUserHome, Path defaultAppPath, int userCount) {
  }

}
