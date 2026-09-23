package com.logiflow.tms.ai.application.outils;

import java.util.Map;
import java.util.Set;

/**
 * Outil métier que le LLM du copilote peut appeler (tool-calling), exécuté par Spring avec les
 * droits de l'utilisateur. Chaque implémentation lit les données via les {@code api} des autres
 * modules — jamais leurs repositories — et renvoie un résultat compact (au plus {@link
 * ArgumentsOutil#LIMITE_MAX} lignes) pour tenir dans le contexte du LLM.
 */
public interface OutilCopilote {

  /** Identifiant snake_case vu par le LLM. */
  String nom();

  /** Libellé affiché à l'utilisateur pendant l'exécution (« Recherche des dossiers… »). */
  String libelle();

  /** Description destinée au LLM : quand utiliser l'outil et ce qu'il renvoie. */
  String description();

  /** JSON Schema (type object) des paramètres. */
  Map<String, Object> parametres();

  /** Rôles (sans préfixe ROLE_) autorisés à utiliser l'outil. */
  Set<String> rolesAutorises();

  ResultatOutil executer(ArgumentsOutil arguments);
}
