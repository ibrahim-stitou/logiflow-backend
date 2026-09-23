package com.logiflow.tms.shared.domain.exception;

/** Détour d'insertion hors itinéraire planifié au-delà du seuil autorisé. */
public class RouteDeviationDepasseeException extends BusinessException {

  public enum PointDeviation {
    PICKUP,
    DROPOFF
  }

  private final PointDeviation point;
  private final double detourKm;
  private final double detourPercent;
  private final double maxAllowedPercent;

  public RouteDeviationDepasseeException(
      PointDeviation point,
      double detourKm,
      double detourPercent,
      double maxAllowedPercent) {
    super(
        String.format(
            "Détour %s de %.1f km (%.1f %%), seuil %.1f %%",
            point == PointDeviation.PICKUP ? "chargement" : "déchargement",
            detourKm,
            detourPercent,
            maxAllowedPercent));
    this.point = point;
    this.detourKm = detourKm;
    this.detourPercent = detourPercent;
    this.maxAllowedPercent = maxAllowedPercent;
  }

  public PointDeviation point() {
    return point;
  }

  public double detourKm() {
    return detourKm;
  }

  public double detourPercent() {
    return detourPercent;
  }

  public double maxAllowedPercent() {
    return maxAllowedPercent;
  }
}
