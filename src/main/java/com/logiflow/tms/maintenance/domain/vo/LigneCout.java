package com.logiflow.tms.maintenance.domain.vo;

import com.logiflow.tms.maintenance.domain.model.TypeLigneCout;
import com.logiflow.tms.shared.domain.vo.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Ligne de coût d'un ordre de travail (main-d'œuvre, pièce, sous-traitance…). Prix unitaire hors
 * taxes ; {@code tauxTva} en pourcentage (20 pour 20 %).
 */
public record LigneCout(
    TypeLigneCout type,
    String designation,
    String referencePiece,
    BigDecimal quantite,
    Money prixUnitaireHt,
    BigDecimal tauxTva) {

  private static final BigDecimal CENT = BigDecimal.valueOf(100);

  public LigneCout {
    Objects.requireNonNull(type, "Le type de ligne est obligatoire");
    Objects.requireNonNull(designation, "La désignation est obligatoire");
    if (designation.isBlank()) {
      throw new IllegalArgumentException("La désignation ne peut pas être vide");
    }
    designation = designation.strip();
    referencePiece =
        referencePiece == null || referencePiece.isBlank() ? null : referencePiece.strip();
    Objects.requireNonNull(quantite, "La quantité est obligatoire");
    if (quantite.signum() <= 0) {
      throw new IllegalArgumentException("La quantité doit être strictement positive");
    }
    Objects.requireNonNull(prixUnitaireHt, "Le prix unitaire est obligatoire");
    if (prixUnitaireHt.montant().signum() < 0) {
      throw new IllegalArgumentException("Le prix unitaire ne peut pas être négatif");
    }
    tauxTva = tauxTva == null ? BigDecimal.valueOf(20) : tauxTva;
    if (tauxTva.signum() < 0 || tauxTva.compareTo(CENT) > 0) {
      throw new IllegalArgumentException("Le taux de TVA doit être compris entre 0 et 100");
    }
  }

  public Money totalHt() {
    return new Money(prixUnitaireHt.montant().multiply(quantite), prixUnitaireHt.devise());
  }

  public Money tva() {
    BigDecimal montant =
        totalHt().montant().multiply(tauxTva).divide(CENT, 2, RoundingMode.HALF_UP);
    return new Money(montant, prixUnitaireHt.devise());
  }

  public Money totalTtc() {
    return totalHt().plus(tva());
  }
}
