package com.logiflow.tms.shared.domain.vo;

import java.util.Objects;

/**
 * Coordonnées géographiques WGS84.
 *
 * @param latitude en degrés décimaux, entre -90 et 90
 * @param longitude en degrés décimaux, entre -180 et 180
 */
public record GeoPoint(double latitude, double longitude) {

  private static final double RAYON_TERRE_KM = 6371.0088;

  public GeoPoint {
    if (latitude < -90 || latitude > 90) {
      throw new IllegalArgumentException("La latitude doit être comprise entre -90 et 90 degrés");
    }
    if (longitude < -180 || longitude > 180) {
      throw new IllegalArgumentException(
          "La longitude doit être comprise entre -180 et 180 degrés");
    }
  }

  /** Distance à vol d'oiseau (formule de Haversine), utilisée pour le préfiltrage du groupage. */
  public double distanceHaversineKm(GeoPoint autre) {
    Objects.requireNonNull(autre, "Le point de destination est obligatoire");
    double dLat = Math.toRadians(autre.latitude - this.latitude);
    double dLon = Math.toRadians(autre.longitude - this.longitude);
    double lat1 = Math.toRadians(this.latitude);
    double lat2 = Math.toRadians(autre.latitude);

    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return RAYON_TERRE_KM * c;
  }
}
