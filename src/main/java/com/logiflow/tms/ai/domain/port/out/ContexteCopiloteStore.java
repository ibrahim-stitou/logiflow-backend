package com.logiflow.tms.ai.domain.port.out;

import com.logiflow.tms.ai.domain.model.copilote.ContexteCopilote;
import java.util.Optional;

/** Stockage éphémère des jetons de contexte du copilote (durée de vie : un message). */
public interface ContexteCopiloteStore {

  void enregistrer(ContexteCopilote contexte);

  /** Contexte valide (non expiré) associé au jeton, vide sinon. */
  Optional<ContexteCopilote> trouver(String jeton);

  void revoquer(String jeton);
}
