package com.logiflow.tms.planning.domain.service;

import com.logiflow.tms.planning.domain.model.ArretVoyage;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Trouve la meilleure position d'insertion d'un point sur l'itinéraire existant d'un voyage.
 *
 * <p>Les distances utilisent la formule de Haversine (à vol d'oiseau). À terme, remplacer par des
 * distances/temps issus d'un moteur de routage (OSRM ou équivalent) pour une précision
 * opérationnelle.
 */
public class InsertionItineraireDomainService {

  private static final double EPSILON_KM = 1e-6;

  public record ResultatInsertion(int apresIndiceArret, double detourKm, double detourPourcent) {}

  /**
   * Évalue chaque tronçon consécutif {@code [arrêt[i], arrêt[i+1]]} et retourne l'insertion après
   * {@code arrêt[i]} qui minimise le détour {@code dist(A,P) + dist(P,B) - dist(A,B)}.
   *
   * @param indiceArretMinimum borne basse inclusive sur {@code apresIndiceArret} (0 pour aucune)
   */
  public ResultatInsertion trouverMeilleureInsertion(
      List<ArretVoyage> arrets, GeoPoint point, int indiceArretMinimum) {
    Objects.requireNonNull(point, "Le point à insérer est obligatoire");
    List<ArretVoyage> arretsOrdonnes =
        arrets.stream().sorted(Comparator.comparingInt(ArretVoyage::indiceSequence)).toList();
    if (arretsOrdonnes.size() < 2) {
      throw new IllegalArgumentException(
          "Au moins deux arrêts sont nécessaires pour calculer une insertion");
    }
    if (indiceArretMinimum < 0) {
      throw new IllegalArgumentException("L'indice d'arrêt minimum ne peut pas être négatif");
    }

    ResultatInsertion meilleur = null;
    for (int i = 0; i < arretsOrdonnes.size() - 1; i++) {
      if (i < indiceArretMinimum) {
        continue;
      }
      ResultatInsertion candidat =
          evaluerInsertion(arretsOrdonnes.get(i), arretsOrdonnes.get(i + 1), i, point);
      if (meilleur == null || candidat.detourKm() < meilleur.detourKm()) {
        meilleur = candidat;
      }
    }
    if (meilleur == null) {
      throw new IllegalArgumentException(
          "Aucune position d'insertion valide après l'indice d'arrêt " + indiceArretMinimum);
    }
    return meilleur;
  }

  public ResultatInsertion trouverMeilleureInsertion(List<ArretVoyage> arrets, GeoPoint point) {
    return trouverMeilleureInsertion(arrets, point, 0);
  }

  public boolean detourAcceptable(double detourPourcent, double seuilMaxPourcent) {
    return detourPourcent <= seuilMaxPourcent;
  }

  private ResultatInsertion evaluerInsertion(
      ArretVoyage depart, ArretVoyage arrivee, int apresIndiceArret, GeoPoint point) {
    GeoPoint localisationDepart = depart.localisation();
    GeoPoint localisationArrivee = arrivee.localisation();
    double distanceDirecteKm = localisationDepart.distanceHaversineKm(localisationArrivee);
    double detourKm =
        localisationDepart.distanceHaversineKm(point)
            + point.distanceHaversineKm(localisationArrivee)
            - distanceDirecteKm;
    detourKm = Math.max(0, detourKm);
    double detourPourcent = pourcentageDetour(detourKm, distanceDirecteKm);
    return new ResultatInsertion(apresIndiceArret, detourKm, detourPourcent);
  }

  private double pourcentageDetour(double detourKm, double distanceDirecteKm) {
    if (distanceDirecteKm < EPSILON_KM) {
      return detourKm < EPSILON_KM ? 0d : Double.POSITIVE_INFINITY;
    }
    return (detourKm / distanceDirecteKm) * 100d;
  }
}
