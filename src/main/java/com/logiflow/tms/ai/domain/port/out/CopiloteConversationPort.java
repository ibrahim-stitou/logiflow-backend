package com.logiflow.tms.ai.domain.port.out;

import com.logiflow.tms.ai.domain.model.copilote.ConversationCopilote;
import com.logiflow.tms.ai.domain.model.copilote.ConversationCopiloteDetail;
import com.logiflow.tms.ai.domain.model.copilote.EvenementCopilote;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import com.logiflow.tms.shared.domain.exception.ServiceIndisponibleException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Port de sortie vers les conversations du copilote, hébergées par le service IA. Toute opération
 * est cloisonnée par {@code utilisateurId} : une conversation d'un autre utilisateur est
 * introuvable ({@link NotFoundException}). Un service IA injoignable lève {@link
 * ServiceIndisponibleException}.
 */
public interface CopiloteConversationPort {

  List<ConversationCopilote> lister(String utilisateurId, int limite, int decalage);

  ConversationCopilote creer(String utilisateurId, String titre);

  ConversationCopiloteDetail obtenir(UUID conversationId, String utilisateurId);

  ConversationCopilote renommer(UUID conversationId, String utilisateurId, String titre);

  void supprimer(UUID conversationId, String utilisateurId);

  void noter(UUID messageId, String utilisateurId, int note, String commentaire);

  /**
   * Envoie une question et relaie, de façon bloquante, chaque événement du flux de réponse au
   * {@code recepteur}. Si le récepteur lève une exception (client déconnecté, bouton Stop), la
   * lecture est interrompue et la connexion au service IA fermée — ce qui interrompt la génération.
   */
  void envoyerMessage(
      UUID conversationId,
      String question,
      String utilisateurId,
      String nomAffichage,
      Set<String> roles,
      String jetonContexte,
      Consumer<EvenementCopilote> recepteur);
}
