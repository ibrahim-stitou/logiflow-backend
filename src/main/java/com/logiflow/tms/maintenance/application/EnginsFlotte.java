package com.logiflow.tms.maintenance.application;

import com.logiflow.tms.fleet.api.RemorqueApi;
import com.logiflow.tms.fleet.api.VehiculeApi;
import com.logiflow.tms.maintenance.domain.model.TypeEngin;
import com.logiflow.tms.maintenance.domain.vo.EnginRef;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Accès aux engins du module {@code fleet} (seul point de contact) : existence, état et compteurs,
 * immobilisation et remise en service, relevé des compteurs à la clôture d'un OT.
 */
@Component
@RequiredArgsConstructor
public class EnginsFlotte {

  private final VehiculeApi vehiculeApi;
  private final RemorqueApi remorqueApi;

  /** État d'un engin : libellé, statut, compteurs et âge. */
  public record EtatEngin(
      EnginRef ref,
      String immatriculation,
      String statut,
      int kilometrage,
      int heures,
      Integer annee) {

    /** Kilométrage moyen journalier depuis la mise en circulation (plafonné, 250 par défaut). */
    public double kmParJourDepuisMiseEnService(LocalDate aujourdHui) {
      if (annee == null || kilometrage <= 0) {
        return 250;
      }
      long jours = Math.max(365, aujourdHui.toEpochDay() - LocalDate.of(annee, 1, 1).toEpochDay());
      return Math.min(900, (double) kilometrage / jours);
    }
  }

  public void verifierExiste(EnginRef engin) {
    boolean existe =
        engin.type() == TypeEngin.VEHICULE
            ? vehiculeApi.consulter(engin.id()).isPresent()
            : remorqueApi.consulter(engin.id()).isPresent();
    if (!existe) {
      throw new NotFoundException(
          (engin.type() == TypeEngin.VEHICULE ? "Aucun véhicule" : "Aucune remorque")
              + " trouvé(e) pour l'identifiant "
              + engin.id());
    }
  }

  /** États de tous les engins en service, indexés par identifiant. */
  public Map<UUID, EtatEngin> etats() {
    Map<UUID, EtatEngin> etats = new HashMap<>();
    vehiculeApi
        .listerPourMaintenance()
        .forEach(
            v ->
                etats.put(
                    v.id(),
                    new EtatEngin(
                        EnginRef.vehicule(v.id()),
                        v.immatriculation(),
                        v.statut(),
                        v.kilometrage(),
                        v.heuresMoteur(),
                        v.anneeMiseEnCirculation())));
    remorqueApi
        .listerPourMaintenance()
        .forEach(
            r ->
                etats.put(
                    r.id(),
                    new EtatEngin(
                        EnginRef.remorque(r.id()),
                        r.immatriculation(),
                        r.statut(),
                        r.kilometrage(),
                        r.heuresGroupeFroid(),
                        r.anneeFabrication())));
    return etats;
  }

  public void immobiliser(EnginRef engin, boolean sinistre) {
    if (engin.type() == TypeEngin.VEHICULE) {
      vehiculeApi.signalerImmobilisation(engin.id(), sinistre);
    } else {
      remorqueApi.signalerImmobilisation(engin.id(), sinistre);
    }
  }

  public void remettreEnService(EnginRef engin) {
    if (engin.type() == TypeEngin.VEHICULE) {
      vehiculeApi.signalerRemiseEnService(engin.id());
    } else {
      remorqueApi.signalerRemiseEnService(engin.id());
    }
  }

  public void releverCompteurs(EnginRef engin, Integer kilometrage, Integer heures) {
    if (kilometrage == null && heures == null) {
      return;
    }
    if (engin.type() == TypeEngin.VEHICULE) {
      vehiculeApi.releverCompteurs(engin.id(), kilometrage, heures);
    } else {
      remorqueApi.releverCompteurs(engin.id(), kilometrage, heures);
    }
  }
}
