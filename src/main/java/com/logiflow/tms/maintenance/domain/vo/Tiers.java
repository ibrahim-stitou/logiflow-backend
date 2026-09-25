package com.logiflow.tms.maintenance.domain.vo;

/** Partie adverse d'un sinistre (tous les champs sont facultatifs sauf le nom). */
public record Tiers(String nom, String immatriculation, String assureur, String numeroPolice) {

  public Tiers {
    if (nom == null || nom.isBlank()) {
      throw new IllegalArgumentException("Le nom du tiers est obligatoire");
    }
    nom = nom.strip();
  }
}
