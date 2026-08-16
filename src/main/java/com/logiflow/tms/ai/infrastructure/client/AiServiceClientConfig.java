package com.logiflow.tms.ai.infrastructure.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/** Client HTTP synchrone dédié aux appels sortants vers le service IA (Flask). */
@Configuration
public class AiServiceClientConfig {

  private static final String HEADER_CLE_INTERNE = "X-Internal-Api-Key";

  @Bean
  public RestClient aiServiceRestClient(AiServiceProperties properties) {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(properties.connectTimeout());
    requestFactory.setReadTimeout(properties.readTimeout());

    return RestClient.builder()
        .baseUrl(properties.baseUrl())
        .requestFactory(requestFactory)
        .defaultHeader(HEADER_CLE_INTERNE, properties.apiKey())
        .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .build();
  }
}
