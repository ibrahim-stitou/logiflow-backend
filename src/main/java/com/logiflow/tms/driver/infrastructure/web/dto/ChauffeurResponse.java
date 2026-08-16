package com.logiflow.tms.driver.infrastructure.web.dto;

import com.logiflow.tms.driver.domain.model.Chauffeur;
import com.logiflow.tms.driver.domain.vo.Habilitation;
import java.util.List;
import java.util.UUID;

public record ChauffeurResponse(
    UUID id,
    String matricule,
    String nomComplet,
    String statut,
    long soldeTempsConduiteMinutes,
    List<Habilitation> habilitations) {

  public static ChauffeurResponse depuis(Chauffeur chauffeur) {
    return new ChauffeurResponse(
        chauffeur.id(),
        chauffeur.matricule(),
        chauffeur.nomComplet(),
        chauffeur.statut().name(),
        chauffeur.soldeTempsConduite().toMinutes(),
        chauffeur.habilitations());
  }
}
