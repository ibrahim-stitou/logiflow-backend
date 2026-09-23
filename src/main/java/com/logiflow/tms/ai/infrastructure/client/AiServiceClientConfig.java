package com.logiflow.tms.ai.infrastructure.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/** Clients HTTP synchrones dédiés aux appels sortants vers le service IA (Flask). */
@Configuration
public class AiServiceClientConfig {

  private static final String HEADER_CLE_INTERNE = "X-Internal-Api-Key";

  @Bean
  @Primary
  public RestClient aiServiceRestClient(AiServiceProperties properties) {
    return client(properties, properties.readTimeout());
  }

  /**
   * Client du flux SSE du copilote : le délai de lecture s'applique entre deux fragments reçus
   * (appels d'outils, premier token d'un LLM local à froid), d'où une valeur plus longue.
   */
  @Bean
  public RestClient aiServiceStreamRestClient(AiServiceProperties properties) {
    return client(properties, properties.streamReadTimeout());
  }

  private static RestClient client(AiServiceProperties properties, java.time.Duration readTimeout) {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(properties.connectTimeout());
    requestFactory.setReadTimeout(readTimeout);

    return RestClient.builder()
        .baseUrl(properties.baseUrl())
        .requestFactory(requestFactory)
        .defaultHeader(HEADER_CLE_INTERNE, properties.apiKey())
        .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .build();
  }
}
