package com.logiflow.tms.referential.infrastructure.persistence.mapper;

import com.logiflow.tms.referential.domain.model.Horaires;
import com.logiflow.tms.referential.domain.model.Site;
import com.logiflow.tms.referential.domain.vo.ContraintesAcces;
import com.logiflow.tms.referential.infrastructure.persistence.entity.SiteEntity;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Traduit entre le modèle de domaine {@link Site} et l'entité JPA {@link SiteEntity}.
 *
 * <p>Contrairement à {@link ClientMapper}, cette classe n'est volontairement pas annotée
 * {@code @Mapper} MapStruct : {@link Site} impose ses invariants via des fabriques statiques (pas
 * de setters JavaBean), et la conversion géométrique (PostGIS/JTS) ainsi que la sérialisation JSON
 * des horaires et contraintes d'accès sortent entièrement du cadre de la génération automatique
 * MapStruct — 100% du mapping est écrit à la main ci-dessous. MapStruct ne génère par ailleurs pas
 * de constructeur déléguant vers une classe abstraite portant une dépendance injectée (testé avec
 * MapStruct 1.6.3), un simple {@code @Component} à injection par constructeur est donc plus correct
 * ici qu'une annotation {@code @Mapper} qui n'apporterait aucune génération de code.
 *
 * <p>L'{@link ObjectMapper} injecté est celui de Jackson 3 ({@code tools.jackson.databind}),
 * utilisé par défaut par {@code spring-boot-starter-web} en Spring Boot 4.1 (voir {@link
 * com.logiflow.tms.config.JacksonConfig}). Ses exceptions ({@link JacksonException}) sont non
 * vérifiées, contrairement à {@code JsonProcessingException} en Jackson 2.
 */
@Component
@RequiredArgsConstructor
public class SiteMapper {

  // TODO vérifier que le tenant courant sera lu depuis le contexte de sécurité une fois le module
  // iam implémenté ; en attendant, un tenant par défaut est utilisé (application mono-tenant).
  private static final UUID TENANT_PAR_DEFAUT =
      UUID.fromString("00000000-0000-0000-0000-000000000000");
  private static final int SRID_WGS84 = 4326;
  private static final GeometryFactory GEOMETRY_FACTORY =
      new GeometryFactory(new PrecisionModel(), SRID_WGS84);

  private final ObjectMapper objectMapper;

  public Site versDomaine(SiteEntity entity) {
    if (entity == null) {
      return null;
    }
    return Site.reconstituer(
        entity.getId(),
        entity.getCode(),
        entity.getLibelle(),
        entity.getClientId(),
        versGeoPoint(entity.getLocalisation()),
        entity.getAdresse(),
        versHoraires(entity.getHorairesJson()),
        versContraintesAcces(entity.getContraintesAccesJson()),
        entity.isActif());
  }

  public SiteEntity versEntite(Site site) {
    if (site == null) {
      return null;
    }
    return SiteEntity.builder()
        .id(site.id())
        .tenantId(TENANT_PAR_DEFAUT)
        .code(site.code())
        .libelle(site.libelle())
        .clientId(site.clientId())
        .localisation(versPoint(site.localisation()))
        .adresse(site.adresse())
        .horairesJson(versJson(site.horaires().creneaux()))
        .contraintesAccesJson(versJson(site.contraintesAcces()))
        .actif(site.estActif())
        .build();
  }

  private Point versPoint(GeoPoint geoPoint) {
    if (geoPoint == null) {
      return null;
    }
    Point point =
        GEOMETRY_FACTORY.createPoint(new Coordinate(geoPoint.longitude(), geoPoint.latitude()));
    point.setSRID(SRID_WGS84);
    return point;
  }

  private GeoPoint versGeoPoint(Point point) {
    if (point == null) {
      return null;
    }
    return new GeoPoint(point.getY(), point.getX());
  }

  private String versJson(Object valeur) {
    try {
      return objectMapper.writeValueAsString(valeur);
    } catch (JacksonException e) {
      throw new IllegalStateException("Échec de sérialisation JSON en persistance", e);
    }
  }

  private Horaires versHoraires(String json) {
    if (json == null || json.isBlank()) {
      return Horaires.aucun();
    }
    try {
      List<Horaires.CreneauHoraire> creneaux =
          objectMapper.readValue(json, new TypeReference<List<Horaires.CreneauHoraire>>() {});
      return Horaires.de(creneaux);
    } catch (JacksonException e) {
      throw new IllegalStateException("Échec de désérialisation des horaires", e);
    }
  }

  private ContraintesAcces versContraintesAcces(String json) {
    if (json == null || json.isBlank()) {
      return ContraintesAcces.aucune();
    }
    try {
      return objectMapper.readValue(json, ContraintesAcces.class);
    } catch (JacksonException e) {
      throw new IllegalStateException("Échec de désérialisation des contraintes d'accès", e);
    }
  }
}
