package com.logiflow.tms.ai.infrastructure.client;

import com.logiflow.tms.ai.domain.model.copilote.ConversationCopilote;
import com.logiflow.tms.ai.domain.model.copilote.ConversationCopiloteDetail;
import com.logiflow.tms.ai.domain.model.copilote.EvenementCopilote;
import com.logiflow.tms.ai.domain.port.out.CopiloteConversationPort;
import com.logiflow.tms.ai.infrastructure.client.dto.CopiloteConversationDto;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import com.logiflow.tms.shared.domain.exception.ServiceIndisponibleException;
import com.logiflow.tms.shared.infrastructure.web.CorrelationIdFilter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Adaptateur HTTP vers les conversations du copilote hébergées par le service IA. Le flux de
 * réponse ({@code text/event-stream}) est lu ligne à ligne et relayé événement par événement ; la
 * fermeture de la connexion (fin de lecture, erreur, client parti) interrompt la génération côté
 * service IA. Voir docs/integration-ia.md.
 */
@Component
public class CopiloteConversationHttpAdapter implements CopiloteConversationPort {

  static final String HEADER_UTILISATEUR = "X-Utilisateur-Id";
  private static final String BASE = "/internal/ai/v1/copilot";

  private final RestClient aiServiceRestClient;
  private final RestClient aiServiceStreamRestClient;

  public CopiloteConversationHttpAdapter(
      @Qualifier("aiServiceRestClient") RestClient aiServiceRestClient,
      @Qualifier("aiServiceStreamRestClient") RestClient aiServiceStreamRestClient) {
    this.aiServiceRestClient = aiServiceRestClient;
    this.aiServiceStreamRestClient = aiServiceStreamRestClient;
  }

  @Override
  public List<ConversationCopilote> lister(String utilisateurId, int limite, int decalage) {
    List<CopiloteConversationDto.Conversation> conversations =
        appeler(
            () ->
                aiServiceRestClient
                    .get()
                    .uri(BASE + "/conversations?limite={l}&decalage={d}", limite, decalage)
                    .header(HEADER_UTILISATEUR, utilisateurId)
                    .retrieve()
                    .onStatus(estIntrouvable(), (req, res) -> introuvable())
                    .body(new ParameterizedTypeReference<>() {}));
    return conversations == null
        ? List.of()
        : conversations.stream().map(CopiloteConversationDto.Conversation::versDomaine).toList();
  }

  @Override
  public ConversationCopilote creer(String utilisateurId, String titre) {
    return conversation(
        appeler(
            () ->
                aiServiceRestClient
                    .post()
                    .uri(BASE + "/conversations")
                    .header(HEADER_UTILISATEUR, utilisateurId)
                    .body(new CopiloteConversationDto.Titre(titre))
                    .retrieve()
                    .body(CopiloteConversationDto.Conversation.class)));
  }

  @Override
  public ConversationCopiloteDetail obtenir(UUID conversationId, String utilisateurId) {
    CopiloteConversationDto.Detail detail =
        appeler(
            () ->
                aiServiceRestClient
                    .get()
                    .uri(BASE + "/conversations/{id}", conversationId)
                    .header(HEADER_UTILISATEUR, utilisateurId)
                    .retrieve()
                    .onStatus(estIntrouvable(), (req, res) -> introuvable())
                    .body(CopiloteConversationDto.Detail.class));
    if (detail == null) {
      throw new ServiceIndisponibleException("Réponse vide du service IA (copilote)");
    }
    return detail.versDomaine();
  }

  @Override
  public ConversationCopilote renommer(UUID conversationId, String utilisateurId, String titre) {
    return conversation(
        appeler(
            () ->
                aiServiceRestClient
                    .patch()
                    .uri(BASE + "/conversations/{id}", conversationId)
                    .header(HEADER_UTILISATEUR, utilisateurId)
                    .body(new CopiloteConversationDto.Titre(titre))
                    .retrieve()
                    .onStatus(estIntrouvable(), (req, res) -> introuvable())
                    .body(CopiloteConversationDto.Conversation.class)));
  }

  @Override
  public void supprimer(UUID conversationId, String utilisateurId) {
    appeler(
        () ->
            aiServiceRestClient
                .delete()
                .uri(BASE + "/conversations/{id}", conversationId)
                .header(HEADER_UTILISATEUR, utilisateurId)
                .retrieve()
                .onStatus(estIntrouvable(), (req, res) -> introuvable())
                .toBodilessEntity());
  }

  @Override
  public void noter(UUID messageId, String utilisateurId, int note, String commentaire) {
    appeler(
        () ->
            aiServiceRestClient
                .post()
                .uri(BASE + "/messages/{id}/feedback", messageId)
                .header(HEADER_UTILISATEUR, utilisateurId)
                .body(new CopiloteConversationDto.Feedback(note, commentaire))
                .retrieve()
                .onStatus(estIntrouvable(), (req, res) -> introuvable())
                .toBodilessEntity());
  }

  @Override
  public void envoyerMessage(
      UUID conversationId,
      String question,
      String utilisateurId,
      String nomAffichage,
      Set<String> roles,
      String jetonContexte,
      Consumer<EvenementCopilote> recepteur) {
    var corps =
        new CopiloteConversationDto.EnvoyerMessage(
            question,
            new CopiloteConversationDto.Utilisateur(
                utilisateurId, nomAffichage, roles.stream().sorted().toList()),
            jetonContexte,
            CorrelationIdFilter.correlationIdCourant());
    appeler(
        () ->
            aiServiceStreamRestClient
                .post()
                .uri(BASE + "/conversations/{id}/messages", conversationId)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .body(corps)
                .exchange(
                    (requete, reponse) -> {
                      HttpStatusCode statut = reponse.getStatusCode();
                      if (statut.value() == HttpStatus.NOT_FOUND.value()) {
                        introuvable();
                      }
                      if (statut.isError()) {
                        throw new ServiceIndisponibleException(
                            "Le service IA (copilote) a répondu " + statut.value());
                      }
                      lireFlux(reponse.getBody(), recepteur);
                      return null;
                    }));
  }

  /**
   * Parse le flux SSE : lignes {@code event:} / {@code data:}, événement émis sur ligne vide. Les
   * données JSON du service IA tiennent toujours sur une ligne.
   */
  static void lireFlux(java.io.InputStream flux, Consumer<EvenementCopilote> recepteur)
      throws IOException {
    try (var lecteur = new BufferedReader(new InputStreamReader(flux, StandardCharsets.UTF_8))) {
      String nom = null;
      StringBuilder donnees = new StringBuilder();
      String ligne;
      while ((ligne = lecteur.readLine()) != null) {
        if (ligne.isEmpty()) {
          if (nom != null && !donnees.isEmpty()) {
            recepteur.accept(new EvenementCopilote(nom, donnees.toString()));
          }
          nom = null;
          donnees.setLength(0);
        } else if (ligne.startsWith("event:")) {
          nom = ligne.substring("event:".length()).strip();
        } else if (ligne.startsWith("data:")) {
          if (!donnees.isEmpty()) {
            donnees.append('\n');
          }
          donnees.append(ligne.substring("data:".length()).stripLeading());
        }
      }
      if (nom != null && !donnees.isEmpty()) {
        recepteur.accept(new EvenementCopilote(nom, donnees.toString()));
      }
    }
  }

  private static <T> T appeler(Supplier<T> appel) {
    try {
      return appel.get();
    } catch (RestClientException e) {
      throw new ServiceIndisponibleException(
          "Le service IA (copilote) est momentanément indisponible", e);
    }
  }

  private static ConversationCopilote conversation(CopiloteConversationDto.Conversation dto) {
    if (dto == null) {
      throw new ServiceIndisponibleException("Réponse vide du service IA (copilote)");
    }
    return dto.versDomaine();
  }

  private static java.util.function.Predicate<HttpStatusCode> estIntrouvable() {
    return statut -> statut.value() == HttpStatus.NOT_FOUND.value();
  }

  private static void introuvable() {
    throw new NotFoundException("Conversation introuvable");
  }
}
