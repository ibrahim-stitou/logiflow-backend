package com.logiflow.tms.ai.application;

import com.logiflow.tms.ai.application.command.EnvoyerMessageCopiloteCommand;
import com.logiflow.tms.ai.domain.model.InteractionIa;
import com.logiflow.tms.ai.domain.model.TypeInteractionIa;
import com.logiflow.tms.ai.domain.model.copilote.ContexteCopilote;
import com.logiflow.tms.ai.domain.model.copilote.ConversationCopilote;
import com.logiflow.tms.ai.domain.model.copilote.ConversationCopiloteDetail;
import com.logiflow.tms.ai.domain.model.copilote.EtatCopilote;
import com.logiflow.tms.ai.domain.model.copilote.EvenementCopilote;
import com.logiflow.tms.ai.domain.port.out.ContexteCopiloteStore;
import com.logiflow.tms.ai.domain.port.out.CopiloteConversationPort;
import com.logiflow.tms.ai.domain.port.out.InteractionIaRepository;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import com.logiflow.tms.shared.domain.exception.ServiceIndisponibleException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Cas d'utilisation du copilote conversationnel (chatbot). Les conversations vivent dans la base du
 * service IA ; Spring authentifie l'utilisateur, relaie les appels et, pour chaque message, émet un
 * jeton de contexte grâce auquel le service IA peut appeler les outils métier ({@code
 * /internal/copilote/**}) avec les droits — et seulement les droits — de cet utilisateur.
 */
@Service
public class CopiloteConversationService {

  static final String PREFIXE_ROLE = "ROLE_";
  private static final SecureRandom ALEATOIRE = new SecureRandom();

  private final CopiloteConversationPort conversationPort;
  private final ContexteCopiloteStore contexteStore;
  private final InteractionIaRepository interactionRepository;
  private final Duration dureeContexte;

  public CopiloteConversationService(
      CopiloteConversationPort conversationPort,
      ContexteCopiloteStore contexteStore,
      InteractionIaRepository interactionRepository,
      @Value("${logiflow.ai-service.contexte-ttl:5m}") Duration dureeContexte) {
    this.conversationPort = conversationPort;
    this.contexteStore = contexteStore;
    this.interactionRepository = interactionRepository;
    this.dureeContexte = dureeContexte;
  }

  public EtatCopilote etat() {
    return conversationPort.etat();
  }

  public List<ConversationCopilote> lister(String utilisateurId, int limite, int decalage) {
    return conversationPort.lister(utilisateurId, limite, decalage);
  }

  public ConversationCopilote creer(String utilisateurId, String titre) {
    return conversationPort.creer(utilisateurId, titre);
  }

  public ConversationCopiloteDetail obtenir(UUID conversationId, String utilisateurId) {
    return conversationPort.obtenir(conversationId, utilisateurId);
  }

  public ConversationCopilote renommer(UUID conversationId, String utilisateurId, String titre) {
    return conversationPort.renommer(conversationId, utilisateurId, titre);
  }

  public void supprimer(UUID conversationId, String utilisateurId) {
    conversationPort.supprimer(conversationId, utilisateurId);
  }

  public void noter(UUID messageId, String utilisateurId, int note, String commentaire) {
    conversationPort.noter(messageId, utilisateurId, note, commentaire);
  }

  /**
   * Relaie la réponse streamée au {@code recepteur}. Ne lève jamais d'exception métier : les échecs
   * (conversation introuvable, service IA indisponible) deviennent un événement {@code erreur},
   * seul canal encore disponible une fois le flux SSE ouvert. Une exception levée par le récepteur
   * lui-même (client déconnecté) interrompt le flux et est propagée.
   */
  public void envoyerMessage(
      EnvoyerMessageCopiloteCommand command, Consumer<EvenementCopilote> recepteur) {
    Instant debut = Instant.now();
    Set<String> roles = sansPrefixe(command.roles());
    ContexteCopilote contexte =
        new ContexteCopilote(
            nouveauJeton(),
            command.utilisateurId(),
            command.nomAffichage(),
            roles,
            debut.plus(dureeContexte));
    contexteStore.enregistrer(contexte);
    boolean succes = false;
    String erreur = null;
    try {
      conversationPort.envoyerMessage(
          command.conversationId(),
          command.question(),
          command.utilisateurId(),
          command.nomAffichage(),
          roles,
          contexte.jeton(),
          recepteur);
      succes = true;
    } catch (NotFoundException e) {
      erreur = e.getMessage();
      recepteur.accept(
          EvenementCopilote.erreur("CONVERSATION_INTROUVABLE", "Conversation introuvable."));
    } catch (ServiceIndisponibleException e) {
      erreur = e.getMessage();
      recepteur.accept(
          EvenementCopilote.erreur(
              "SERVICE_INDISPONIBLE",
              "Le copilote est momentanément indisponible. Réessayez dans quelques instants."));
    } catch (RuntimeException e) {
      erreur = "Flux interrompu : " + e.getClass().getSimpleName();
      throw e;
    } finally {
      contexteStore.revoquer(contexte.jeton());
      journaliser(command, succes, debut, erreur);
    }
  }

  static Set<String> sansPrefixe(Set<String> roles) {
    return roles.stream()
        .map(role -> role.startsWith(PREFIXE_ROLE) ? role.substring(PREFIXE_ROLE.length()) : role)
        .collect(Collectors.toUnmodifiableSet());
  }

  private static String nouveauJeton() {
    byte[] octets = new byte[32];
    ALEATOIRE.nextBytes(octets);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(octets);
  }

  private void journaliser(
      EnvoyerMessageCopiloteCommand command, boolean succes, Instant debut, String erreur) {
    // Métadonnées seulement : le contenu des échanges est conservé par le service IA.
    interactionRepository.sauvegarder(
        InteractionIa.enregistrer(
            UUID.randomUUID(),
            TypeInteractionIa.COPILOTE,
            command.utilisateurId(),
            succes,
            Duration.between(debut, Instant.now()).toMillis(),
            "conversation=" + command.conversationId(),
            erreur));
  }
}
