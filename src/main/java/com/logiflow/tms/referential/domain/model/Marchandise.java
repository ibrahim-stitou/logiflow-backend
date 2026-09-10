package com.logiflow.tms.referential.domain.model;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Marchandise du catalogue référentiel : type de bien transportable, sélectionné par les lignes de
 * commande et de dossier de transport (poids et volume restent propres à chaque ligne).
 */
public final class Marchandise {

  private final UUID id;
  private final String code;
  private String libelle;
  private String famille;
  private String classeAdr;
  private String numeroOnu;
  private boolean gerbable;
  private boolean actif;

  private Marchandise(
      UUID id,
      String code,
      String libelle,
      String famille,
      String classeAdr,
      String numeroOnu,
      boolean gerbable,
      boolean actif) {
    this.id = Objects.requireNonNull(id, "L'identifiant de la marchandise est obligatoire");
    this.code = validerCode(code);
    this.libelle = validerLibelle(libelle);
    this.famille = famille;
    this.classeAdr = classeAdr;
    this.numeroOnu = numeroOnu;
    this.gerbable = gerbable;
    this.actif = actif;
  }

  public static Marchandise creer(
      UUID id,
      String code,
      String libelle,
      String famille,
      String classeAdr,
      String numeroOnu,
      boolean gerbable) {
    return new Marchandise(id, code, libelle, famille, classeAdr, numeroOnu, gerbable, true);
  }

  public static Marchandise reconstituer(
      UUID id,
      String code,
      String libelle,
      String famille,
      String classeAdr,
      String numeroOnu,
      boolean gerbable,
      boolean actif) {
    return new Marchandise(id, code, libelle, famille, classeAdr, numeroOnu, gerbable, actif);
  }

  public void renommer(String libelle) {
    this.libelle = validerLibelle(libelle);
  }

  public void activer() {
    this.actif = true;
  }

  public void desactiver() {
    this.actif = false;
  }

  /** Une marchandise est classée matière dangereuse dès qu'elle porte une classe ADR. */
  public boolean estDangereuse() {
    return classeAdr != null && !classeAdr.isBlank();
  }

  private static String validerCode(String code) {
    Objects.requireNonNull(code, "Le code de la marchandise est obligatoire");
    String normalise = code.strip().toUpperCase(Locale.ROOT);
    if (normalise.isEmpty()) {
      throw new IllegalArgumentException("Le code de la marchandise ne peut pas être vide");
    }
    return normalise;
  }

  private static String validerLibelle(String libelle) {
    Objects.requireNonNull(libelle, "Le libellé de la marchandise est obligatoire");
    if (libelle.isBlank()) {
      throw new IllegalArgumentException("Le libellé de la marchandise ne peut pas être vide");
    }
    return libelle.strip();
  }

  public UUID id() {
    return id;
  }

  public String code() {
    return code;
  }

  public String libelle() {
    return libelle;
  }

  public String famille() {
    return famille;
  }

  public String classeAdr() {
    return classeAdr;
  }

  public String numeroOnu() {
    return numeroOnu;
  }

  public boolean gerbable() {
    return gerbable;
  }

  public boolean estActif() {
    return actif;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Marchandise marchandise)) {
      return false;
    }
    return id.equals(marchandise.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
