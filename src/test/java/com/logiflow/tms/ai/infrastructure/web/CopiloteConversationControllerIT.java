package com.logiflow.tms.ai.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.logiflow.tms.shared.AbstractIntegrationTest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Relais du copilote vers le service IA, simulé par un serveur HTTP local qui imite les routes
 * Flask (/internal/ai/v1/copilot/**), y compris le flux {@code text/event-stream}.
 */
@AutoConfigureMockMvc
class CopiloteConversationControllerIT extends AbstractIntegrationTest {

  private static final UUID CONVERSATION = UUID.fromString("7d4c4d2e-2b0c-4b8e-9d7c-111111111111");
  private static final HttpServer SERVICE_IA;
  private static final List<Map<String, String>> REQUETES = new CopyOnWriteArrayList<>();
  private static final Map<String, String> CORPS = new ConcurrentHashMap<>();

  static {
    try {
      SERVICE_IA = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    SERVICE_IA.createContext("/internal/ai/v1/copilot", CopiloteConversationControllerIT::repondre);
    SERVICE_IA.createContext(
        "/health",
        echange ->
            envoyer(
                echange,
                200,
                "application/json",
                "{\"status\":\"UP\",\"dependances\":{\"llm\":\"MODELE_ABSENT\",\"base\":\"UP\"},\"modele\":\"llama-3.3-70b-versatile\",\"fournisseur\":\"api.groq.com\"}"));
    SERVICE_IA.start();
  }

  @DynamicPropertySource
  static void serviceIa(DynamicPropertyRegistry registry) {
    registry.add(
        "logiflow.ai-service.base-url",
        () -> "http://127.0.0.1:" + SERVICE_IA.getAddress().getPort());
  }

  @AfterAll
  static void arreter() {
    SERVICE_IA.stop(0);
  }

  @Autowired private MockMvc mockMvc;

  @BeforeEach
  void vider() {
    REQUETES.clear();
    CORPS.clear();
  }

  private static void repondre(HttpExchange echange) throws IOException {
    String chemin = echange.getRequestURI().getPath();
    String corps = new String(echange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    REQUETES.add(
        Map.of(
            "methode", echange.getRequestMethod(),
            "chemin", chemin,
            "cle", String.valueOf(echange.getRequestHeaders().getFirst("X-Internal-Api-Key")),
            "utilisateur",
                String.valueOf(echange.getRequestHeaders().getFirst("X-Utilisateur-Id"))));
    CORPS.put(chemin, corps);

    String conversation =
        """
        {"id":"%s","titre":"Voyages","createdAt":"2026-09-23T10:00:00Z","updatedAt":"2026-09-23T10:00:00Z"}"""
            .formatted(CONVERSATION);
    if (chemin.endsWith("/conversations") && "POST".equals(echange.getRequestMethod())) {
      envoyer(echange, 201, "application/json", conversation);
    } else if (chemin.endsWith("/conversations")) {
      envoyer(echange, 200, "application/json", "[" + conversation + "]");
    } else if (chemin.endsWith("/" + CONVERSATION + "/messages")) {
      envoyer(
          echange,
          200,
          "text/event-stream",
          """
          event: meta
          data: {"conversationId":"%s","messageId":"m1"}

          event: outil
          data: {"nom":"rechercher_voyages","libelle":"Recherche des voyages","statut":"debut"}

          event: token
          data: {"texte":"VOY-2026-00003 est en cours."}

          event: fin
          data: {"messageId":"m1"}

          """
              .formatted(CONVERSATION));
    } else if (chemin.endsWith("/" + CONVERSATION)) {
      envoyer(
          echange,
          200,
          "application/json",
          conversation.substring(0, conversation.length() - 1)
              + ",\"messages\":[{\"id\":\"%s\",\"role\":\"assistant\",\"contenu\":\"Bonjour\",\"statut\":\"complet\",\"sources\":[{\"type\":\"VOYAGE\",\"reference\":\"VOY-1\",\"id\":null}],\"createdAt\":\"2026-09-23T10:00:01Z\"}]}"
                  .formatted(UUID.randomUUID()));
    } else {
      envoyer(echange, 404, "application/json", "{\"title\":\"Conversation introuvable\"}");
    }
  }

  private static void envoyer(HttpExchange echange, int statut, String type, String corps)
      throws IOException {
    byte[] octets = corps.getBytes(StandardCharsets.UTF_8);
    echange.getResponseHeaders().add("Content-Type", type);
    echange.sendResponseHeaders(statut, octets.length);
    echange.getResponseBody().write(octets);
    echange.close();
  }

  @Test
  void creerUneConversationRelaieLUtilisateurAuthentifie() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/ia/copilote/conversations")
                .with(jwt().jwt(j -> j.subject("user-42")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(CONVERSATION.toString()))
        .andExpect(jsonPath("$.titre").value("Voyages"));

    assertThat(REQUETES.getFirst())
        .containsEntry("utilisateur", "user-42")
        .containsEntry("cle", "local-dev-key");
  }

  @Test
  void lEtatDuCopiloteRefleteCeluiDuServiceIa() throws Exception {
    mockMvc
        .perform(get("/api/v1/ia/copilote/etat").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.serviceIa").value(true))
        .andExpect(jsonPath("$.llm").value("MODELE_ABSENT"))
        .andExpect(jsonPath("$.fournisseur").value("api.groq.com"))
        .andExpect(jsonPath("$.modele").value("llama-3.3-70b-versatile"))
        .andExpect(jsonPath("$.operationnel").value(false));
  }

  @Test
  void consulterUneConversationRenvoieSesMessagesEtSources() throws Exception {
    mockMvc
        .perform(get("/api/v1/ia/copilote/conversations/" + CONVERSATION).with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.messages[0].contenu").value("Bonjour"))
        .andExpect(jsonPath("$.messages[0].sources[0].reference").value("VOY-1"));
  }

  @Test
  void uneConversationInconnueRenvoie404() throws Exception {
    mockMvc
        .perform(get("/api/v1/ia/copilote/conversations/" + UUID.randomUUID()).with(jwt()))
        .andExpect(status().isNotFound());
  }

  @Test
  void envoyerUnMessageRelaieLeFluxSseEtTransmetUnJetonDeContexte() throws Exception {
    MvcResult resultat =
        mockMvc
            .perform(
                post("/api/v1/ia/copilote/conversations/" + CONVERSATION + "/messages")
                    .with(
                        jwt()
                            .jwt(j -> j.subject("user-42").claim("preferred_username", "alice"))
                            .authorities(
                                new org.springframework.security.core.authority
                                    .SimpleGrantedAuthority("ROLE_EXPLOITANT")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"question\": \"Quels voyages sont en cours ?\"}"))
            .andExpect(request().asyncStarted())
            .andReturn();
    resultat.getAsyncResult(10_000);

    String flux = resultat.getResponse().getContentAsString(StandardCharsets.UTF_8);
    assertThat(flux)
        .contains("event:meta")
        .contains("event:outil")
        .contains("VOY-2026-00003 est en cours.")
        .contains("event:fin");
    String corps = CORPS.get("/internal/ai/v1/copilot/conversations/" + CONVERSATION + "/messages");
    assertThat(corps)
        .contains("\"question\":\"Quels voyages sont en cours ?\"")
        .contains("\"id\":\"user-42\"")
        .contains("\"nom\":\"alice\"")
        .contains("\"roles\":[\"EXPLOITANT\"]")
        .containsPattern("\"contexte\":\"[A-Za-z0-9_-]{40,}\"");
  }

  @Test
  void envoyerDansUneConversationInconnueEmetUnEvenementErreur() throws Exception {
    MvcResult resultat =
        mockMvc
            .perform(
                post("/api/v1/ia/copilote/conversations/" + UUID.randomUUID() + "/messages")
                    .with(jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"question\": \"Bonjour\"}"))
            .andExpect(request().asyncStarted())
            .andReturn();
    resultat.getAsyncResult(10_000);

    assertThat(resultat.getResponse().getContentAsString(StandardCharsets.UTF_8))
        .contains("event:erreur")
        .contains("CONVERSATION_INTROUVABLE");
  }

  @Test
  void uneQuestionVideRenvoie400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/ia/copilote/conversations/" + CONVERSATION + "/messages")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"question\": \"\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void uneNoteNulleRenvoie400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/ia/copilote/messages/" + UUID.randomUUID() + "/feedback")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"note\": 0}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void sansAuthentificationLAccesEstRefuse() throws Exception {
    // Profil test permissif (pas de resource server JWT) : refus anonyme en 403 ; 401 en dev/prod.
    mockMvc
        .perform(get("/api/v1/ia/copilote/conversations"))
        .andExpect(status().is4xxClientError());
  }
}
