package com.logiflow.tms.planning.application;

import com.logiflow.tms.driver.api.ChauffeurApi;
import com.logiflow.tms.driver.api.dto.ChauffeurPlanificationSummary;
import com.logiflow.tms.fleet.api.RemorqueApi;
import com.logiflow.tms.fleet.api.VehiculeApi;
import com.logiflow.tms.fleet.api.dto.RemorquePlanificationSummary;
import com.logiflow.tms.fleet.api.dto.VehiculePlanificationSummary;
import com.logiflow.tms.planning.domain.model.Voyage;
import com.logiflow.tms.planning.domain.port.out.VoyageRepository;
import com.logiflow.tms.planning.domain.vo.Affectation;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Occupation des ressources (véhicules, remorques, chauffeurs) par les voyages actifs sur une
 * période : la disponibilité se juge sur la période du voyage, pas sur le statut du jour.
 */
@Service
@RequiredArgsConstructor
public class DisponibiliteRessourcesService {

  private static final ZoneId FUSEAU_EXPLOITATION = ZoneId.of("Europe/Paris");
  private static final Set<String> STATUTS_HORS_EXPLOITATION =
      Set.of("EN_MAINTENANCE", "IMMOBILISE", "HORS_SERVICE");

  private final VoyageRepository voyageRepository;
  private final VehiculeApi vehiculeApi;
  private final RemorqueApi remorqueApi;
  private final ChauffeurApi chauffeurApi;

  /** Ressources libres et exploitables sur une période, pour la planification manuelle. */
  public record RessourcesDisponibles(
      List<VehiculePlanificationSummary> vehicules,
      List<RemorquePlanificationSummary> remorques,
      List<ChauffeurPlanificationSummary> chauffeurs) {}

  /**
   * Ressources mobilisées sur [debut, fin], chaque ressource associée à la référence d'un voyage
   * qui l'occupe. {@code voyageIgnore} (optionnel) est exclu du calcul.
   */
  public record RessourcesOccupees(
      Map<UUID, String> vehicules, Map<UUID, String> remorques, Map<UUID, String> chauffeurs) {

    public RessourcesOccupees {
      vehicules = Map.copyOf(vehicules);
      remorques = Map.copyOf(remorques);
      chauffeurs = Map.copyOf(chauffeurs);
    }

    public Set<UUID> tous() {
      var ids = new java.util.HashSet<UUID>(vehicules.keySet());
      ids.addAll(remorques.keySet());
      ids.addAll(chauffeurs.keySet());
      return ids;
    }
  }

  /**
   * Véhicules et remorques en exploitation aux documents valides, chauffeurs sans motif générique
   * de non-affectation, non engagés sur un autre voyage pendant [debut, fin].
   */
  @Transactional(readOnly = true)
  public RessourcesDisponibles ressourcesDisponibles(Instant debut, Instant fin) {
    if (!fin.isAfter(debut)) {
      throw new BusinessException("La fin de période doit être postérieure au début");
    }
    LocalDate date = LocalDate.ofInstant(debut, FUSEAU_EXPLOITATION);
    RessourcesOccupees occupees = ressourcesOccupees(debut, fin, null);
    return new RessourcesDisponibles(
        vehiculeApi.listerPourPlanification(date).stream()
            .filter(v -> !STATUTS_HORS_EXPLOITATION.contains(v.statut()))
            .filter(VehiculePlanificationSummary::documentsValides)
            .filter(v -> !occupees.vehicules().containsKey(v.id()))
            .toList(),
        remorqueApi.listerPourPlanification(date).stream()
            .filter(r -> !STATUTS_HORS_EXPLOITATION.contains(r.statut()))
            .filter(RemorquePlanificationSummary::documentsValides)
            .filter(r -> !occupees.remorques().containsKey(r.id()))
            .toList(),
        chauffeurApi.listerPourPlanification(date).stream()
            .filter(c -> c.motifsNonAffectation().isEmpty())
            .filter(c -> !occupees.chauffeurs().containsKey(c.id()))
            .toList());
  }

  @Transactional(readOnly = true)
  public RessourcesOccupees ressourcesOccupees(Instant debut, Instant fin, UUID voyageIgnore) {
    Map<UUID, String> vehicules = new HashMap<>();
    Map<UUID, String> remorques = new HashMap<>();
    Map<UUID, String> chauffeurs = new HashMap<>();
    List<Voyage> voyages = voyageRepository.actifsSurPeriode(debut, fin);
    for (Voyage voyage : voyages) {
      if (Objects.equals(voyage.id(), voyageIgnore)) {
        continue;
      }
      String reference = voyage.reference().valeur();
      vehicules.putIfAbsent(voyage.vehiculeId(), reference);
      if (voyage.remorqueId() != null) {
        remorques.putIfAbsent(voyage.remorqueId(), reference);
      }
      for (Affectation affectation : voyage.affectations()) {
        chauffeurs.putIfAbsent(affectation.chauffeurId(), reference);
      }
    }
    return new RessourcesOccupees(vehicules, remorques, chauffeurs);
  }
}
