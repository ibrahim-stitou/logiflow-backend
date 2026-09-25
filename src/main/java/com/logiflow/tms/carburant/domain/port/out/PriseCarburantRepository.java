package com.logiflow.tms.carburant.domain.port.out;

import com.logiflow.tms.carburant.domain.model.PriseCarburant;
import com.logiflow.tms.carburant.domain.model.StatutPrise;
import com.logiflow.tms.carburant.domain.model.TypeCarburant;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PriseCarburantRepository {

  PriseCarburant sauvegarder(PriseCarburant prise);

  Optional<PriseCarburant> parId(UUID id);

  Page<PriseCarburant> rechercher(
      String texteRecherche, UUID voyageId, StatutPrise statut, PageRequest pageRequest);

  PriseCarburantStats stats(String texteRecherche, UUID voyageId, StatutPrise statut);

  /** Agrégat des prises d'un véhicule (ou de toute la flotte si null) sur [debut, fin[. */
  PriseCarburantStats statsPeriode(UUID vehiculeId, Instant debut, Instant fin);

  record PriseCarburantStats(
      long nombre, double litresTotal, BigDecimal montantTotal, List<ParType> parType) {}

  record ParType(TypeCarburant type, long nombre, double litres, BigDecimal montant) {}
}
