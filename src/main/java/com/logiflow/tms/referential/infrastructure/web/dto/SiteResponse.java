package com.logiflow.tms.referential.infrastructure.web.dto;

import com.logiflow.tms.referential.domain.model.Horaires;
import com.logiflow.tms.referential.domain.model.Site;
import com.logiflow.tms.referential.domain.vo.ContraintesAcces;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.util.List;
import java.util.UUID;

/** Représentation publique d'un site. Ne jamais exposer l'entité JPA directement. */
public record SiteResponse(
    UUID id,
    String code,
    String libelle,
    UUID clientId,
    GeoPoint localisation,
    String adresse,
    ContraintesAcces contraintesAcces,
    List<Horaires.CreneauHoraire> horaires,
    boolean actif) {

  public static SiteResponse depuis(Site site) {
    return new SiteResponse(
        site.id(),
        site.code(),
        site.libelle(),
        site.clientId(),
        site.localisation(),
        site.adresse(),
        site.contraintesAcces(),
        site.horaires().creneaux(),
        site.estActif());
  }
}
