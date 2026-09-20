package com.logiflow.tms.ai.domain.port.out;

import com.logiflow.tms.ai.domain.model.PointItineraire;
import com.logiflow.tms.shared.domain.exception.ServiceIndisponibleException;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.util.List;

/** Résolution de la géométrie routière (polyligne) entre des arrêts ordonnés. */
public interface RouteGeometryPort {

  /**
   * Retourne les coordonnées WGS84 le long des routes empruntées, dans l'ordre du trajet.
   *
   * @throws ServiceIndisponibleException si le moteur de routage est injoignable
   */
  List<GeoPoint> resoudreGeometrie(List<PointItineraire> points);
}
