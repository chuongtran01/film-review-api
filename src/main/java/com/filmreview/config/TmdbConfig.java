package com.filmreview.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for TMDB API.
 */
@Configuration
public class TmdbConfig {

  @Value("${tmdb.api-key}")
  private String apiKey;

  @Value("${tmdb.image-base-url}")
  private String imageBaseUrl;

  public String getApiKey() {
    return apiKey;
  }

  public String getImageBaseUrl() {
    return imageBaseUrl;
  }
}
