package com.logiflow.tms.dossier.infrastructure.persistence.entity;

import com.logiflow.tms.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * Entité JPA d'une ligne de marchandise de dossier, isolée du modèle de domaine {@link
 * com.logiflow.tms.dossier.domain.vo.LigneMarchandise}.
 */
@Getter
@Entity
@Table(name = "ligne_marchandise", schema = "dossier")
public class LigneMarchandiseEntity extends BaseEntity {

  @Column(name = "dossier_id", nullable = false)
  private UUID dossierId;

  @Column(name = "marchandise_id", nullable = false)
  private UUID marchandiseId;

  @Column(name = "poids_kg", nullable = false)
  private double poidsKg;

  @Column(name = "volume_m3", nullable = false)
  private double volumeM3;

  @Column(name = "nb_colis", nullable = false)
  private int nbColis;

  @Column(name = "classe_adr", length = 20)
  private String classeAdr;

  @Column(name = "numero_onu", length = 20)
  private String numeroOnu;

  @Column(name = "gerbable")
  private Boolean gerbable;

  protected LigneMarchandiseEntity() {}

  @Builder
  public LigneMarchandiseEntity(
      UUID id,
      UUID dossierId,
      UUID marchandiseId,
      double poidsKg,
      double volumeM3,
      int nbColis,
      String classeAdr,
      String numeroOnu,
      Boolean gerbable) {
    definirId(id);
    this.dossierId = dossierId;
    this.marchandiseId = marchandiseId;
    this.poidsKg = poidsKg;
    this.volumeM3 = volumeM3;
    this.nbColis = nbColis;
    this.classeAdr = classeAdr;
    this.numeroOnu = numeroOnu;
    this.gerbable = gerbable;
  }
}
