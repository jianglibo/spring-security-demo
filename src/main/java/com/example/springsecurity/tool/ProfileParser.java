package com.example.springsecurity.tool;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ProfileParser {

  @Autowired
  Environment environment;

  public String deployRegion() {
    String prefix = "region-";
    for (String profile : environment.getActiveProfiles()) {
      if (profile.startsWith(prefix)) {
        return profile.substring(prefix.length());
      }
    }
    return "";
  }

  public boolean isSlave() {
    for (String profile : environment.getActiveProfiles()) {
      if (profile.startsWith("slave")) {
        return true;
      }
    }
    return false;
  }

  public boolean isDev() {
    for (String profile : environment.getActiveProfiles()) {
      if (profile.equals("dev")) {
        return true;
      }
    }
    return false;
  }
}
