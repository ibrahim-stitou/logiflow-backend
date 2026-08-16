package com.logiflow.tms.planning.infrastructure.persistence.entity;

import com.logiflow.tms.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * Entité JPA du voyage, isolée du modèle de domaine {@link
 * com.logiflow.tms.planning.domain.model.Voyage}.
 */
@Getter
@Entity
@Table(
    name = "voyage",
    schema = "planning",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_voyage_tenant_reference",
            columnNames = {"tenant_id", "reference"}))
public class VoyageEntity extends BaseEntity {

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "reference", nullable = false, length = 20)
  private String reference;

  @Column(name = "type_voyage", nullable = false, length = 20)
  private String typeVoyage;

  @Column(name = "portee", nullable = false, length = 20)
  private String portee;

  @Column(name = "statut", nullable = false, length = 20)
  private String statut;

  @Column(name = "depart_prevu", nullable = false)
  private Instant departPrevu;

  @Column(name = "arrivee_prevue", nullable = false)
  private Instant arriveePrevue;

  @Column(name = "vehicule_id", nullable = false)
  private UUID vehiculeId;

  @Column(name = "remorque_id")
  private UUID remorqueId;

  @Column(name = "dossier_ids_json", nullable = false, columnDefinition = "text")
  private String dossierIdsJson;

  @Column(name = "trajet_json", nullable = false, columnDefinition = "text")
  private String trajetJson;

  @Column(name = "affectations_json", nullable = false, columnDefinition = "text")
  private String affectationsJson;

  @Column(name = "taux_remplissage", nullable = false)
  private double tauxRemplissage;

  protected VoyageEntity() {}

  @Builder
  public VoyageEntity(
      UUID id,
      UUID tenantId,
      String reference,
      String typeVoyage,
      String portee,
      String statut,
      Instant departPrevu,
      Instant arriveePrevue,
      UUID vehiculeId,
      UUID remorqueId,
      String dossierIdsJson,
      String trajetJson,
      String affectationsJson,
      double tauxRemplissage) {
    definirId(id);
    this.tenantId = tenantId;
    this.reference = reference;
    this.typeVoyage = typeVoyage;
    this.portee = portee;
    this.statut = statut;
    this.departPrevu = departPrevu;
    this.arriveePrevue = arriveePrevue;
    this.vehiculeId = vehiculeId;
    this.remorqueId = remorqueId;
    this.dossierIdsJson = dossierIdsJson;
    this.trajetJson = trajetJson;
    this.affectationsJson = affectationsJson;
    this.tauxRemplissage = tauxRemplissage;
  }
}
