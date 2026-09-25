package com.logiflow.tms.config;

import jakarta.servlet.DispatcherType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Socle de sécurité de LogiFlow : resource server OAuth2/JWT, sans état, deny-by-default.
 *
 * <p>Prêt pour Keycloak (issuer/JWK configurés via les propriétés standard {@code
 * spring.security.oauth2.resourceserver.jwt.*}), mais l'application démarre sans fournisseur
 * d'identité en profil {@code local}/{@code test} ({@code logiflow.security.permissive-local-
 * profile=true}) : la configuration {@code oauth2ResourceServer().jwt()} est alors entièrement
 * omise (aucun {@code JwtDecoder} n'est requis). En profil {@code local} uniquement, {@code
 * logiflow.security.anonymous-local-access=true} installe un filtre qui authentifie un utilisateur
 * fictif pour que le frontend Angular puisse appeler l'API. Les tests (profil {@code test})
 * n'activent pas ce filtre et injectent un JWT via {@code
 * SecurityMockMvcRequestPostProcessors.jwt()}. Dès qu'un IdP est configuré (profil {@code
 * dev}/prod), le JWT redevient obligatoire pour toute route non publique.
 *
 * <p>DSL lambda et {@link JwtGrantedAuthoritiesConverter} vérifiés compatibles avec Spring Security
 * 7.1.0 (Spring Boot 4.1) par compilation effective face aux artefacts réels.
 */
@Configuration
public class SecurityConfig {

  private static final String[] PUBLIC_ENDPOINTS = {
    "/actuator/health",
    "/actuator/health/**",
    "/actuator/info",
    "/v3/api-docs",
    "/v3/api-docs/**",
    "/swagger-ui.html",
    "/swagger-ui/**"
  };

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      CorsConfigurationSource corsConfigurationSource,
      LogiflowProperties properties)
      throws Exception {
    boolean permissiveLocalProfile = properties.security().permissiveLocalProfile();

    if (properties.security().anonymousLocalAccess()) {
      http.addFilterBefore(new LocalDevAuthenticationFilter(), AnonymousAuthenticationFilter.class);
    }

    http.cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    // STATELESS : le SecurityContext ne survit pas au dispatch ERROR/FORWARD vers
                    // /error. Sans ceci, toute exception MVC (validation, 404, etc.) rebondit en
                    // 403 vide au lieu du ProblemDetail. Idem pour le dispatch ASYNC qui clôt un
                    // flux
                    // SSE (copilote) : la requête d'origine a déjà été autorisée.
                    .dispatcherTypeMatchers(
                        DispatcherType.FORWARD,
                        DispatcherType.ERROR,
                        DispatcherType.INCLUDE,
                        DispatcherType.ASYNC)
                    .permitAll()
                    .requestMatchers(PUBLIC_ENDPOINTS)
                    .permitAll()
                    .anyRequest()
                    .authenticated());

    if (permissiveLocalProfile) {
      // Sans resource server, Spring Security retombe sur Http403ForbiddenEntryPoint : une requête
      // non authentifiée recevrait 403. On garde la sémantique du mode JWT (401 = pas authentifié,
      // 403 = authentifié sans les droits).
      http.exceptionHandling(
          exceptions ->
              exceptions.authenticationEntryPoint(
                  new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
    } else {
      // Aucun IdP configuré en mode permissif : ne pas appeler oauth2ResourceServer().jwt() du
      // tout, sinon Spring Security exige quand même un bean JwtDecoder au démarrage.
      http.oauth2ResourceServer(
          (OAuth2ResourceServerConfigurer<HttpSecurity> oauth2) ->
              oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
    }

    http.headers(
        headers ->
            headers
                .contentTypeOptions(withDefaults -> {})
                .frameOptions(frameOptions -> frameOptions.deny())
                .referrerPolicy(
                    referrer ->
                        referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)));

    return http.build();
  }

  /**
   * Convertit les rôles portés par le claim {@code roles} (ou {@code realm_access.roles} au format
   * Keycloak) du JWT en {@link GrantedAuthority} Spring Security préfixées {@code ROLE_}.
   */
  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();
    defaultConverter.setAuthorityPrefix("ROLE_");
    defaultConverter.setAuthoritiesClaimName("roles");

    Converter<Jwt, Collection<GrantedAuthority>> rolesConverter =
        jwt -> {
          Collection<GrantedAuthority> authorities = new ArrayList<>(defaultConverter.convert(jwt));
          authorities.addAll(extractKeycloakRealmRoles(jwt));
          return authorities;
        };

    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(rolesConverter);
    return converter;
  }

  /**
   * Valideur MFA branché automatiquement sur le {@code JwtDecoder} auto-configuré (Spring Boot 4
   * collecte les beans {@code OAuth2TokenValidator<Jwt>}) : dès que {@code
   * logiflow.security.mfa.required=true}, tout jeton ne prouvant pas une étape MFA (claims OIDC
   * {@code amr}/{@code acr}) est rejeté en 401. Voir {@link MfaJwtValidator}.
   */
  @Bean
  public OAuth2TokenValidator<Jwt> mfaJwtValidator(LogiflowProperties properties) {
    return new MfaJwtValidator(properties);
  }

  private List<GrantedAuthority> extractKeycloakRealmRoles(Jwt jwt) {
    var realmAccess = jwt.getClaimAsMap("realm_access");
    if (realmAccess == null || !(realmAccess.get("roles") instanceof Collection<?> roles)) {
      return List.of();
    }
    return roles.stream()
        .filter(Objects::nonNull)
        .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
        .collect(Collectors.toList());
  }
}
