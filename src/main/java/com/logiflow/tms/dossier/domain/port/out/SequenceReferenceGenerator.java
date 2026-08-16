package com.logiflow.tms.dossier.domain.port.out;

import com.logiflow.tms.shared.domain.vo.Reference;

/**
 * Port de sortie pour la génération de références séquentielles de dossier. Implémenté en
 * infrastructure par un adaptateur vers le générateur technique partagé.
 */
public interface SequenceReferenceGenerator {

  Reference generer(String prefixe, int annee);
}
