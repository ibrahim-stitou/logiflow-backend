package com.logiflow.tms.referential.infrastructure.persistence.entity;

import com.logiflow.tms.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * Entité JPA de la marchandise, isolée du modèle de domaine {@link
 * com.logiflow.tms.referential.domain.model.Marchandise}.
 */
@Getter
@Entity
@Table(
    name = "marchandise",
    schema = "referential",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_marchandise_code",
            columnNames = {"code"}))
public class MarchandiseEntity extends BaseEntity {

  @Column(name = "code", nullable = false, length = 50)
  private String code;

  @Column(name = "libelle", nullable = false, length = 255)
  private String libelle;

  @Column(name = "famille", length = 100)
  private String famille;

  @Column(name = "classe_adr", length = 20)
  private String classeAdr;

  @Column(name = "numero_onu", length = 20)
  private String numeroOnu;

  @Column(name = "gerbable", nullable = false)
  private boolean gerbable;

  @Column(name = "actif", nullable = false)
  private boolean actif;

  protected MarchandiseEntity() {}

  @Builder
  public MarchandiseEntity(
      UUID id,
      String code,
      String libelle,
      String famille,
      String classeAdr,
      String numeroOnu,
      boolean gerbable,
      boolean actif) {
    definirId(id);
    this.code = code;
    this.libelle = libelle;
    this.famille = famille;
    this.classeAdr = classeAdr;
    this.numeroOnu = numeroOnu;
    this.gerbable = gerbable;
    this.actif = actif;
  }
}
