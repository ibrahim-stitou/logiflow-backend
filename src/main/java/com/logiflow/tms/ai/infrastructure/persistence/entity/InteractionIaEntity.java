package com.logiflow.tms.ai.infrastructure.persistence.entity;

import com.logiflow.tms.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * Entité JPA du journal des interactions IA, isolée du modèle de domaine {@link
 * com.logiflow.tms.ai.domain.model.InteractionIa}.
 */
@Getter
@Entity
@Table(name = "interaction_ia", schema = "ai")
public class InteractionIaEntity extends BaseEntity {

  @Column(name = "type_interaction", nullable = false, length = 20)
  private String typeInteraction;

  @Column(name = "utilisateur_id", length = 100)
  private String utilisateurId;

  @Column(name = "succes", nullable = false)
  private boolean succes;

  @Column(name = "duree_ms", nullable = false)
  private long dureeMs;

  @Column(name = "resume", length = 2000)
  private String resume;

  @Column(name = "erreur", length = 2000)
  private String erreur;

  protected InteractionIaEntity() {}

  @Builder
  public InteractionIaEntity(
      UUID id,
      String typeInteraction,
      String utilisateurId,
      boolean succes,
      long dureeMs,
      String resume,
      String erreur) {
    definirId(id);
    this.typeInteraction = typeInteraction;
    this.utilisateurId = utilisateurId;
    this.succes = succes;
    this.dureeMs = dureeMs;
    this.resume = resume;
    this.erreur = erreur;
  }
}
