package com.logiflow.tms.ai.application.outils;

import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.ValidationException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Arguments d'un appel d'outil, produits par le LLM : tolérants sur la forme (nombre en chaîne,
 * casse d'une valeur énumérée, chaîne vide = absent) mais stricts sur le fond. Toute valeur
 * invalide lève une {@link ValidationException} (HTTP 400) dont le message, renvoyé au LLM, lui
 * permet de corriger son appel.
 */
public final class ArgumentsOutil {

  public static final int LIMITE_PAR_DEFAUT = 10;
  public static final int LIMITE_MAX = 20;

  private final Map<String, Object> valeurs;

  public ArgumentsOutil(Map<String, Object> valeurs) {
    this.valeurs = valeurs != null ? Map.copyOf(sansNulls(valeurs)) : Map.of();
  }

  private static Map<String, Object> sansNulls(Map<String, Object> valeurs) {
    return valeurs.entrySet().stream()
        .filter(e -> e.getKey() != null && e.getValue() != null)
        .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /** Texte optionnel, {@code null} si absent ou vide. */
  public String texte(String nom) {
    Object valeur = valeurs.get(nom);
    if (valeur == null) {
      return null;
    }
    String texte = valeur.toString().strip();
    if (texte.length() > 100) {
      throw invalide(nom, "100 caractères maximum");
    }
    return texte.isEmpty() ? null : texte;
  }

  /** Valeur énumérée optionnelle, normalisée en majuscules. */
  public String enumere(String nom, Collection<String> valeursPossibles) {
    String texte = texte(nom);
    if (texte == null) {
      return null;
    }
    String normalise = texte.toUpperCase(Locale.ROOT).replace(' ', '_');
    if (!valeursPossibles.contains(normalise)) {
      throw invalide(nom, "valeurs possibles : " + String.join(", ", valeursPossibles));
    }
    return normalise;
  }

  /** Date optionnelle au format ISO (AAAA-MM-JJ). */
  public LocalDate date(String nom) {
    String texte = texte(nom);
    if (texte == null) {
      return null;
    }
    try {
      return LocalDate.parse(texte);
    } catch (DateTimeParseException e) {
      throw invalide(nom, "format attendu AAAA-MM-JJ");
    }
  }

  /** Nombre de résultats demandé, borné à [1, {@link #LIMITE_MAX}]. */
  public int limite() {
    Object valeur = valeurs.get("limite");
    if (valeur == null || valeur.toString().isBlank()) {
      return LIMITE_PAR_DEFAUT;
    }
    try {
      int limite = (int) Double.parseDouble(valeur.toString());
      return Math.clamp(limite, 1, LIMITE_MAX);
    } catch (NumberFormatException e) {
      throw invalide("limite", "entier attendu");
    }
  }

  public PageRequest premierePage() {
    return PageRequest.premiere(limite());
  }

  private static ValidationException invalide(String nom, String detail) {
    return new ValidationException(
        "Argument « %s » invalide : %s.".formatted(nom, detail), List.of(nom + ": " + detail));
  }
}
