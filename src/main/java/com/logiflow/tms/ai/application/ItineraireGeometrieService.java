package com.logiflow.tms.ai.application;

import com.logiflow.tms.ai.domain.model.PointItineraire;
import com.logiflow.tms.ai.domain.port.out.RouteGeometryPort;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItineraireGeometrieService {

  private final RouteGeometryPort routeGeometryPort;

  public List<GeoPoint> calculer(List<PointItineraire> points) {
    return routeGeometryPort.resoudreGeometrie(points);
  }
}
