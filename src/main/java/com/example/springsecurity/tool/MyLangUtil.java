package com.example.springsecurity.tool;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class MyLangUtil {

  private static Pattern allDigits = Pattern.compile("\\d+");

  public static String[] combineStringArray(String baseValue, String... params) {
    int totalLength = 1 + params.length; // 1 for the baseValue
    String[] combinedArray = new String[totalLength];

    combinedArray[0] = baseValue;
    System.arraycopy(params, 0, combinedArray, 1, params.length);

    return combinedArray;
  }

  public static boolean isAllDigits(String str) {
    if (str == null) {
      return false;
    }
    return allDigits.matcher(str).matches();
  }

  /**
   * If str is null or blank.
   * 
   * @param str
   * @return boolean
   */
  public static boolean isBlank(String str) {
    return str == null || str.trim().isBlank();
  }

  public static boolean isNotBlank(String str) {
    return !isBlank(str);
  }

  public static String blankToEmpty(String str) {
    return isBlank(str) ? "" : str;
  }

  private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

  public static String bytesToHex(byte[] bytes) {
    byte[] hexChars = new byte[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = HEX_ARRAY[v >>> 4];
      hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars, StandardCharsets.UTF_8);
  }

  public static byte[] hex2Bytes(String hex) {
    byte[] bytes = new byte[hex.length() / 2];
    for (int i = 0; i < bytes.length; i++) {
      int index = i * 2;
      int j = Integer.parseInt(hex.substring(index, index + 2), 16);
      bytes[i] = (byte) j;
    }
    return bytes;
  }

  /**
   * String template. using ${} to replace variables.
   */
  public static class StringTemplate {

    private String template;
    private Map<String, Object> variables;

    public StringTemplate(String template) {
      this.template = template;
      this.variables = new HashMap<>();
    }

    public void setVariable(String name, Object value) {
      variables.put(name, value);
    }

    public String render() {
      String result = template;
      for (Map.Entry<String, Object> entry : variables.entrySet()) {
        result = result.replace("${" + entry.getKey() + "}", entry.getValue().toString());
      }
      return result;
    }
  }


  public static final Pattern PATTERN_DATETIME = Pattern.compile("\\.\\d*");

  public static OffsetDateTime paserOffsetDatetimeLoosely(String datetimeString) {
    // Find the fractional seconds part and remove it
    Matcher matcher = PATTERN_DATETIME.matcher(datetimeString);
    if (matcher.find()) {
      datetimeString = datetimeString.replace(matcher.group(), "");
    }

    // Define a DateTimeFormatter for the modified datetime string
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    // Parse the modified datetime string
    return OffsetDateTime.parse(datetimeString, formatter);
  }

  public static Boolean parameterTrueFalse(String pb) {
    return "1".equals(pb) || "true".equalsIgnoreCase(pb);
  }

  /**
   * compare the two invoking of this method to ensure the string wasn't changed.
   * 
   * @param secret
   * @param str
   * @return
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   */
  public String signHMACSHA1(String secret, String str)
      throws NoSuchAlgorithmException, InvalidKeyException {
    Mac hmacSha1 = Mac.getInstance("HmacSHA1");
    SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
    hmacSha1.init(keySpec);
    byte[] hash = hmacSha1.doFinal(str.getBytes(StandardCharsets.UTF_8));
    String hex = bytesToHex(hash);
    return hex;
  }

  public static String sha256(InputStream inputStream) {
    MessageDigest sha256;
    try {
      sha256 = MessageDigest.getInstance("SHA-256");
      byte[] buffer = new byte[8192];
      int bytesRead;

      while ((bytesRead = inputStream.read(buffer)) != -1) {
        sha256.update(buffer, 0, bytesRead);
      }

      byte[] hash = sha256.digest();
      StringBuilder hexString = new StringBuilder();

      for (byte b : hash) {
        String hex = String.format("%02x", b);
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException | IOException e) {
      throw new RuntimeException("create sha256sum failed.", e);
    }

  }

  public static String sha256(java.nio.file.Path filePath) {
    try (InputStream fileInputStream = Files.newInputStream(filePath)) {
      return sha256(fileInputStream);
    } catch (IOException e) {
      throw new RuntimeException("create sha256sum failed.", e);
    }
  }

  public static Long stringToLong(String input) {
    return stringToInteger(input) + 0L;
  }

  public static int stringToIntegerMaybeNegative(String input) {
    try {
      // Create a MessageDigest instance with the SHA-256 algorithm
      MessageDigest md = MessageDigest.getInstance("SHA-256");

      // Compute the hash value of the input string
      byte[] hashBytes = md.digest(input.getBytes());

      // Convert the hash bytes to an integer
      int hashCode = 0;
      for (byte b : hashBytes) {
        hashCode = (hashCode << 8) | (b & 0xFF);
      }

      return hashCode;
    } catch (NoSuchAlgorithmException e) {
      // Handle the exception (e.g., fallback to a default value)
      e.printStackTrace();
      return 0; // Default value
    }
  }

  public static int stringToInteger(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = md.digest(input.getBytes());

      int hashCode = 0;
      for (byte b : hashBytes) {
        hashCode = (hashCode << 8) | (b & 0xFF);
      }

      // Clear the sign bit to make it positive
      hashCode &= 0x7FFFFFFF;

      return hashCode;
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return 0;
    }
  }

}
