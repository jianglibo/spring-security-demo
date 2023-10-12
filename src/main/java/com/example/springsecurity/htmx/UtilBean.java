package com.example.springsecurity.htmx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class UtilBean {

  @Autowired
  ObjectMapper objectMapper;

  public static String formatFileSize(Long fileSize) {
    if (fileSize <= 0) {
      return "0 B";
    }

    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
    int digitGroups = (int) (Math.log10(fileSize) / Math.log10(1024));

    return String.format("%.1f %s", fileSize / Math.pow(1024, digitGroups), units[digitGroups]);
  }

  /**
   * python|php|shell|thymeleaf|html|javascript|css|xml|json|yaml|properties|sql
   */
  public static List<String> supportedCodeLanguages() {
    return List.of("html", "json", "java");
  }

  public String calSortby(String inParam, String field) {
    if (field.equals(inParam)) {
      return "-" + field;
    } else if (("-" + field).equals(inParam)) {
      return field;
    } else {
      return field;
    }
  }

  public <T> List<List<T>> listOflist(List<T> list, int sublistSize) {
    List<List<T>> result = new ArrayList<>();
    for (int i = 0; i < list.size(); i += sublistSize) {
      int endIndex = Math.min(i + sublistSize, list.size());
      result.add(list.subList(i, endIndex));
    }
    return result;
  }

  public static class Kv {
    public String k;
    public String v;

    public Kv(String k, String v) {
      this.k = k;
      this.v = v;
    }
  }

  public String map2json(Map<String, Object> map) throws JsonProcessingException {
    return objectMapper.writeValueAsString(map);
  }

}
