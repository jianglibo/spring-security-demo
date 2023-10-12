package com.example.springsecurity.app;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;

import com.example.springsecurity.app.ClasspathAssetsService.Bundle;

public class ModifiableTemplateResource extends AbstractResource {

  private final String[] NEED_MODIFY =
      new String[] {"classpath:/templates/layouts/blayout.html"};

  private final ClasspathAssetsService classpathAssetsService;

  private final Resource resource;
  private final String resourceName;

  /**
   * @param resource
   */
  public ModifiableTemplateResource(ClasspathAssetsService classpathAssetsService,
      Resource resource, String resourceName) {
    this.resource = resource;
    this.resourceName = resourceName;
    this.classpathAssetsService = classpathAssetsService;
  }

  @Override
  public String getDescription() {
    return resource.getDescription();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    if (!needModify(resourceName)) {
      return resource.getInputStream();
    }
    String content = convertInputStreamToString(resource.getInputStream());
    String modifiedContent = modifyContent(content);
    return new ByteArrayInputStream(modifiedContent.getBytes("UTF-8"));
  }

  public String convertInputStreamToString(InputStream inputStream) {
    try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
      StringBuilder stringBuilder = new StringBuilder();
      while (scanner.hasNextLine()) {
        stringBuilder.append(scanner.nextLine());
      }
      return stringBuilder.toString();
    }
  }

  // modify the content according to the resourceName
  private String modifyContent(String content) {
    Document doc = Jsoup.parse(content);
    Element head = doc.head();
    Elements scripts = head.select("script[data-combine='true']");
    Elements links = head.select("link[data-combine='true']");

    List<String> scriptSrcs = new ArrayList<>();
    scripts.forEach(s -> {
      scriptSrcs.add(s.attr("src"));
    });
    Bundle bundle = classpathAssetsService.uniqueBundle(scriptSrcs, true);
    // Add a new script element
    Element newScript = doc.createElement("script");
    newScript.attr("src", bundle.getUrl());
    head.appendChild(newScript);
    scripts.remove();

    List<String> linkHref = new ArrayList<>();
    links.forEach(l -> {
      linkHref.add(l.attr("href"));
    });
    bundle = classpathAssetsService.uniqueBundle(linkHref, false);
    // Add a new link element
    Element newLink = doc.createElement("link");
    newLink.attr("href", bundle.getUrl());
    newLink.attr("rel", "stylesheet");
    head.appendChild(newLink);
    links.remove();
    return doc.html();
  }

  @Override
  public String getFilename() {
    return resource.getFilename();
  }

  @Override
  public boolean exists() {
    return resource.exists();
  }

  @Override
  public Resource createRelative(String relativePath) throws IOException {
    return resource.createRelative(relativePath);
  }

  private boolean needModify(String resourceName) {
    for (String s : NEED_MODIFY) {
      if (s.equals(resourceName)) {
        return true;
      }
    }
    return false;
  }


}
