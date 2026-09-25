package com.logiflow.tms.maintenance.infrastructure.web.dto;

import com.logiflow.tms.maintenance.domain.model.NatureIntervention;
import com.logiflow.tms.maintenance.domain.model.OrdreTravail;
import com.logiflow.tms.maintenance.domain.model.PrioriteOT;
import com.logiflow.tms.maintenance.domain.model.TypeEngin;
import com.logiflow.tms.maintenance.domain.model.TypeIntervention;
import com.logiflow.tms.maintenance.domain.model.TypeLigneCout;
import com.logiflow.tms.maintenance.domain.vo.EnginRef;
import com.logiflow.tms.maintenance.domain.vo.LigneCout;
import com.logiflow.tms.shared.domain.vo.Money;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Fragments de requêtes et de réponses partagés par les contrôleurs du module maintenance. */
public final class MaintenanceDtos {

  private MaintenanceDtos() {}

  public static Money eur(BigDecimal montant) {
    return montant == null ? null : new Money(montant, OrdreTravail.DEVISE);
  }

  public static BigDecimal montant(Money money) {
    return money == null ? null : money.montant();
  }

  /** Engin désigné par son type et son identifiant. */
  public record EnginDto(@NotNull TypeEngin type, @NotNull UUID id) {

    public EnginRef versRef() {
      return new EnginRef(type, id);
    }

    public static EnginDto depuis(EnginRef ref) {
      return ref == null ? null : new EnginDto(ref.type(), ref.id());
    }
  }

  /** Données descriptives d'un ordre de travail. */
  public record DetailsOTRequest(
      @NotNull TypeIntervention type,
      @NotNull NatureIntervention nature,
      PrioriteOT priorite,
      @NotBlank String titre,
      String description,
      UUID prestataireId,
      @NotNull LocalDateTime debutPlanifie,
      LocalDateTime finPlanifiee,
      Boolean immobilisation,
      @PositiveOrZero BigDecimal budgetEstime) {

    public OrdreTravail.DetailsOT versDetails() {
      return new OrdreTravail.DetailsOT(
          type,
          nature,
          priorite,
          titre,
          description,
          prestataireId,
          debutPlanifie,
          finPlanifiee,
          immobilisation == null || immobilisation,
          eur(budgetEstime));
    }
  }

  /** Ligne de coût (prix unitaire HT, TVA en %). */
  public record LigneCoutDto(
      @NotNull TypeLigneCout type,
      @NotBlank String designation,
      String referencePiece,
      @NotNull @Positive BigDecimal quantite,
      @NotNull @PositiveOrZero BigDecimal prixUnitaireHt,
      BigDecimal tauxTva,
      BigDecimal totalHt,
      BigDecimal totalTtc) {

    public LigneCout versLigne() {
      return new LigneCout(
          type, designation, referencePiece, quantite, eur(prixUnitaireHt), tauxTva);
    }

    public static LigneCoutDto depuis(LigneCout l) {
      return new LigneCoutDto(
          l.type(),
          l.designation(),
          l.referencePiece(),
          l.quantite(),
          l.prixUnitaireHt().montant(),
          l.tauxTva(),
          l.totalHt().montant(),
          l.totalTtc().montant());
    }

    public static List<LigneCout> versLignes(List<LigneCoutDto> lignes) {
      return lignes == null ? List.of() : lignes.stream().map(LigneCoutDto::versLigne).toList();
    }
  }
}
