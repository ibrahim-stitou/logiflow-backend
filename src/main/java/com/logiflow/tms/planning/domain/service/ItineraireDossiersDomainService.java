package com.logiflow.tms.planning.domain.service;

import com.logiflow.tms.planning.domain.model.ArretVoyage;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Construit les arrêts ordonnés d'un voyage à partir des points de chargement et de déchargement de
 * ses dossiers, et rattache chaque dossier à ses deux arrêts.
 *
 * <p>Deux modes : un ordre de sites imposé (itinéraire choisi par l'exploitant ou proposé par
 * l'agent de planification), ou un ordre déduit des fenêtres horaires (chargement avant
 * déchargement pour un même dossier). Des passages consécutifs sur un même site sont fusionnés en
 * un seul arrêt.
 */
public class ItineraireDossiersDomainService {

  /** Points requis par un dossier : site et début de fenêtre du chargement et du déchargement. */
  public record PointsDossier(
      UUID dossierId,
      String reference,
      UUID siteChargementId,
      Instant debutChargement,
      UUID siteDechargementId,
      Instant debutDechargement) {}

  /** Site connu du référentiel, avec ses coordonnées. */
  public record SitePlanifie(UUID siteId, String libelle, GeoPoint localisation) {}

  /** Arrêts affectés à un dossier et leur rang dans l'itinéraire. */
  public record ArretsDossier(
      UUID arretChargementId,
      UUID arretDechargementId,
      int indiceChargement,
      int indiceDechargement) {}

  /** Itinéraire construit ; {@code erreurs} non vide = itinéraire inutilisable. */
  public record Resultat(
      List<ArretVoyage> arrets, Map<UUID, ArretsDossier> arretsParDossier, List<String> erreurs) {

    public boolean valide() {
      return erreurs.isEmpty();
    }
  }

  private record Passage(UUID siteId, Instant instant, int rang) {}

  public Resultat construire(
      UUID voyageId,
      List<PointsDossier> dossiers,
      Map<UUID, SitePlanifie> sites,
      List<UUID> ordreSitesImpose) {
    List<String> erreurs = new ArrayList<>();
    for (PointsDossier dossier : dossiers) {
      verifierSite(dossier.siteChargementId(), "chargement", dossier, sites, erreurs);
      verifierSite(dossier.siteDechargementId(), "déchargement", dossier, sites, erreurs);
    }
    if (ordreSitesImpose != null) {
      for (UUID siteId : ordreSitesImpose) {
        if (!sites.containsKey(siteId)) {
          erreurs.add("Site d'arrêt introuvable dans le référentiel : " + siteId);
        }
      }
    }
    if (!erreurs.isEmpty()) {
      return new Resultat(List.of(), Map.of(), erreurs);
    }

    List<UUID> ordreSites =
        ordreSitesImpose != null && !ordreSitesImpose.isEmpty()
            ? fusionnerConsecutifs(ordreSitesImpose)
            : ordreDepuisFenetres(dossiers);

    List<ArretVoyage> arrets = new ArrayList<>(ordreSites.size());
    for (int i = 0; i < ordreSites.size(); i++) {
      SitePlanifie site = sites.get(ordreSites.get(i));
      arrets.add(
          ArretVoyage.creer(
              UUID.randomUUID(),
              voyageId,
              i,
              site.libelle(),
              site.localisation(),
              site.siteId(),
              true));
    }

    Map<UUID, ArretsDossier> parDossier = new LinkedHashMap<>();
    for (PointsDossier dossier : dossiers) {
      int indiceChargement = premierIndice(ordreSites, dossier.siteChargementId(), 0);
      int indiceDechargement =
          indiceChargement < 0
              ? -1
              : premierIndice(ordreSites, dossier.siteDechargementId(), indiceChargement + 1);
      if (indiceChargement < 0) {
        erreurs.add(
            "L'itinéraire ne dessert pas le site de chargement du dossier " + dossier.reference());
      } else if (indiceDechargement < 0) {
        erreurs.add(
            "L'itinéraire ne dessert pas le site de déchargement du dossier "
                + dossier.reference()
                + " après son chargement");
      } else {
        parDossier.put(
            dossier.dossierId(),
            new ArretsDossier(
                arrets.get(indiceChargement).id(),
                arrets.get(indiceDechargement).id(),
                indiceChargement,
                indiceDechargement));
      }
    }
    if (arrets.size() < 2) {
      erreurs.add("L'itinéraire doit comporter au moins deux arrêts distincts");
    }
    return new Resultat(List.copyOf(arrets), parDossier, List.copyOf(erreurs));
  }

  private static void verifierSite(
      UUID siteId,
      String nature,
      PointsDossier dossier,
      Map<UUID, SitePlanifie> sites,
      List<String> erreurs) {
    if (siteId == null) {
      erreurs.add("Le dossier " + dossier.reference() + " n'a pas de point de " + nature);
    } else if (!sites.containsKey(siteId)) {
      erreurs.add(
          "Le site de " + nature + " du dossier " + dossier.reference() + " est introuvable");
    }
  }

  /**
   * Ordre des passages par début de fenêtre. Un déchargement n'est jamais placé avant le chargement
   * du même dossier ; à instant égal, les chargements passent d'abord.
   */
  private static List<UUID> ordreDepuisFenetres(List<PointsDossier> dossiers) {
    List<Passage> passages = new ArrayList<>();
    for (PointsDossier dossier : dossiers) {
      Instant chargement = dossier.debutChargement();
      Instant dechargement =
          dossier.debutDechargement().isAfter(chargement)
              ? dossier.debutDechargement()
              : chargement;
      passages.add(new Passage(dossier.siteChargementId(), chargement, 0));
      passages.add(new Passage(dossier.siteDechargementId(), dechargement, 1));
    }
    passages.sort(Comparator.comparing(Passage::instant).thenComparingInt(Passage::rang));
    return fusionnerConsecutifs(passages.stream().map(Passage::siteId).toList());
  }

  private static List<UUID> fusionnerConsecutifs(List<UUID> sites) {
    List<UUID> resultat = new ArrayList<>();
    for (UUID site : sites) {
      if (resultat.isEmpty() || !resultat.getLast().equals(site)) {
        resultat.add(site);
      }
    }
    return resultat;
  }

  private static int premierIndice(List<UUID> sites, UUID siteId, int aPartirDe) {
    for (int i = aPartirDe; i < sites.size(); i++) {
      if (sites.get(i).equals(siteId)) {
        return i;
      }
    }
    return -1;
  }
}
