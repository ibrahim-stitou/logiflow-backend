package com.logiflow.tms.shared.domain.exception;

import java.util.List;
import java.util.UUID;

/** Capacité remorque insuffisante sur au moins un tronçon de l'itinéraire. */
public class RemorqueCapaciteDepasseeException extends BusinessException {

  public record TronconEnEchec(
      UUID arretDepartId,
      UUID arretArriveeId,
      String motif,
      Double depassementKg,
      Double depassementM3) {}

  private final List<TronconEnEchec> tronconsEnEchec;

  public RemorqueCapaciteDepasseeException(List<TronconEnEchec> tronconsEnEchec) {
    super("Capacité remorque dépassée sur " + tronconsEnEchec.size() + " tronçon(s)");
    this.tronconsEnEchec = List.copyOf(tronconsEnEchec);
  }

  public List<TronconEnEchec> tronconsEnEchec() {
    return tronconsEnEchec;
  }
}
