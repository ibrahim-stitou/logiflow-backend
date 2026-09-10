package com.logiflow.tms.order.infrastructure.persistence.entity;

import com.logiflow.tms.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * Entité JPA de la commande, isolée du modèle de domaine {@link
 * com.logiflow.tms.order.domain.model.Commande}. Nommée dans le schéma {@code commande} (le module
 * {@code order} évite le mot réservé SQL ORDER).
 */
@Getter
@Entity
@Table(
    name = "commande",
    schema = "commande",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_commande_reference",
            columnNames = {"reference"}))
public class CommandeEntity extends BaseEntity {

  @Column(name = "reference", nullable = false, length = 20)
  private String reference;

  @Column(name = "client_id", nullable = false)
  private UUID clientId;

  @Column(name = "statut", nullable = false, length = 20)
  private String statut;

  @Column(name = "date_souhaitee", nullable = false)
  private LocalDate dateSouhaitee;

  @Column(name = "prix_montant", nullable = false, precision = 12, scale = 2)
  private BigDecimal prixMontant;

  @Column(name = "prix_devise", nullable = false, length = 3)
  private String prixDevise;

  protected CommandeEntity() {}

  @Builder
  public CommandeEntity(
      UUID id,
      String reference,
      UUID clientId,
      String statut,
      LocalDate dateSouhaitee,
      BigDecimal prixMontant,
      String prixDevise) {
    definirId(id);
    this.reference = reference;
    this.clientId = clientId;
    this.statut = statut;
    this.dateSouhaitee = dateSouhaitee;
    this.prixMontant = prixMontant;
    this.prixDevise = prixDevise;
  }
}
