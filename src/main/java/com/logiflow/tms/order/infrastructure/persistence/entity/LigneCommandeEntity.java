package com.logiflow.tms.order.infrastructure.persistence.entity;

import com.logiflow.tms.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * Entité JPA d'une ligne de marchandise de commande, isolée du modèle de domaine {@link
 * com.logiflow.tms.order.domain.vo.LigneCommande}.
 */
@Getter
@Entity
@Table(name = "ligne_commande", schema = "commande")
public class LigneCommandeEntity extends BaseEntity {

  @Column(name = "commande_id", nullable = false)
  private UUID commandeId;

  @Column(name = "marchandise_id", nullable = false)
  private UUID marchandiseId;

  @Column(name = "poids_kg", nullable = false)
  private double poidsKg;

  @Column(name = "volume_m3", nullable = false)
  private double volumeM3;

  @Column(name = "nb_colis", nullable = false)
  private int nbColis;

  protected LigneCommandeEntity() {}

  @Builder
  public LigneCommandeEntity(
      UUID id, UUID commandeId, UUID marchandiseId, double poidsKg, double volumeM3, int nbColis) {
    definirId(id);
    this.commandeId = commandeId;
    this.marchandiseId = marchandiseId;
    this.poidsKg = poidsKg;
    this.volumeM3 = volumeM3;
    this.nbColis = nbColis;
  }
}
