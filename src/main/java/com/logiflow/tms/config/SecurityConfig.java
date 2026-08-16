package com.logiflow.tms.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Socle de sécurité de LogiFlow : resource server OAuth2/JWT, sans état, deny-by-default.
 *
 * <p>Prêt pour Keycloak (issuer/JWK configurés via les propriétés standard {@code
 * spring.security.oauth2.resourceserver.jwt.*}), mais l'application démarre sans fournisseur
 * d'identité en profil {@code local} : seuls les endpoints publics restent accessibles, le reste de
 * l'API exige un JWT valide dès qu'un IdP est configuré.
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
      HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(PUBLIC_ENDPOINTS)
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            (OAuth2ResourceServerConfigurer<HttpSecurity> oauth2) ->
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
        .headers(
            headers ->
                headers
                    .contentTypeOptions(withDefaults -> {})
                    .frameOptions(frameOptions -> frameOptions.deny())
                    .referrerPolicy(
                        referrer ->
                            referrer.policy(
                                ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)));

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
