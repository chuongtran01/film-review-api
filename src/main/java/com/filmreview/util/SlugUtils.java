package com.filmreview.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Utility class for generating URL-friendly slugs from text.
 */
public class SlugUtils {

  private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
  private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

  private SlugUtils() {
    // Utility class - prevent instantiation
  }

  /**
   * Generate URL-friendly slug from text.
   * 
   * @param text      The text to convert to a slug
   * @param maxLength Maximum length of the slug (defaults to 500 if null or <= 0)
   * @return URL-friendly slug
   */
  public static String generateSlug(String text, Integer maxLength) {
    if (text == null || text.isEmpty()) {
      return "untitled";
    }

    String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
    String slug = NON_LATIN.matcher(normalized).replaceAll("");
    slug = WHITESPACE.matcher(slug).replaceAll("-");
    slug = slug.toLowerCase();

    int max = (maxLength != null && maxLength > 0) ? maxLength : 500;
    return slug.length() > max ? slug.substring(0, max) : slug;
  }

  /**
   * Generate URL-friendly slug from text with default max length of 500.
   * 
   * @param text The text to convert to a slug
   * @return URL-friendly slug
   */
  public static String generateSlug(String text) {
    return generateSlug(text, 500);
  }
}
