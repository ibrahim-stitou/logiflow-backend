package com.logiflow.tms.ai.domain.port.out;

import com.logiflow.tms.ai.domain.model.InteractionIa;

/** Port de sortie pour la persistance du journal des interactions IA. */
public interface InteractionIaRepository {

  InteractionIa sauvegarder(InteractionIa interaction);
}
