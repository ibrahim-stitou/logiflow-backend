package com.logiflow.tms.dossier.domain.model;

/**
 * Type de carrosserie requis par la marchandise d'un dossier.
 *
 * <p>Vocabulaire propre au module {@code dossier}, volontairement distinct de {@code
 * fleet.domain.model.TypeCarrosserie} : un module ne dépend jamais du domaine d'un autre (règle
 * d'architecture 5), même quand les valeurs se recoupent. Le rapprochement entre les deux se fait
 * dans la couche application du module {@code planning} au moment de l'affectation.
 */
public enum TypeCarrosserieRequise {
  TAUTLINER,
  FRIGORIFIQUE,
  CITERNE,
  PLATEAU,
  BENNE,
  PORTE_CONTENEUR
}
