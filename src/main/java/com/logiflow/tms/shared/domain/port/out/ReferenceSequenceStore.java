package com.logiflow.tms.shared.domain.port.out;

/** Dernière séquence persistée pour un préfixe et une année donnés. */
public interface ReferenceSequenceStore {

  long derniereSequence(String prefixe, int annee);
}
