package com.logiflow.tms.fleet.application;

import com.logiflow.tms.document.api.DocumentApi;
import com.logiflow.tms.fleet.api.VehiculeApi;
import com.logiflow.tms.fleet.api.dto.VehiculeSummary;
import com.logiflow.tms.fleet.application.command.CreerVehiculeCommand;
import com.logiflow.tms.fleet.domain.model.StatutVehicule;
import com.logiflow.tms.fleet.domain.model.Vehicule;
import com.logiflow.tms.fleet.domain.port.out.VehiculeRepository;
import com.logiflow.tms.fleet.domain.service.FleetDomainService;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import com.logiflow.tms.shared.domain.vo.Immatriculation;
import com.logiflow.tms.shared.domain.vo.Poids;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cas d'utilisation applicatifs du sous-domaine Véhicule. */
@Service
@RequiredArgsConstructor
public class VehiculeService implements VehiculeApi {

  // Doit correspondre à TypeEntiteDocumentable.VEHICULE du module document (contrat en String pour
  // ne pas exposer ce type de domaine hors de son module).
  private static final String TYPE_ENTITE_DOCUMENTABLE = "VEHICULE";

  private final VehiculeRepository vehiculeRepository;
  private final FleetDomainService fleetDomainService;
  private final DocumentApi documentApi;

  @Transactional
  public UUID creerVehicule(CreerVehiculeCommand command) {
    Immatriculation immatriculation = new Immatriculation(command.immatriculation());
    fleetDomainService.verifierImmatriculationDisponible(
        immatriculation.valeur(),
        vehiculeRepository.existeParImmatriculation(immatriculation.valeur()));
    Vehicule vehicule =
        Vehicule.creer(
            UUID.randomUUID(),
            immatriculation,
            command.type(),
            command.numeroParc(),
            command.vin(),
            command.marque(),
            command.modele(),
            command.anneeMiseEnCirculation(),
            command.energie(),
            new Poids(command.ptacKg()),
            command.poidsVideKg() != null ? new Poids(command.poidsVideKg()) : null,
            new Poids(command.chargeUtileKg()),
            command.longueurM(),
            command.largeurM(),
            command.hauteurM(),
            command.volumeUtileM3(),
            command.nbPositionsPalettes(),
            command.typeCarrosserie(),
            command.groupeFroid(),
            command.temperatureMin(),
            command.temperatureMax(),
            command.datePremiereMiseCirculation(),
            command.dateAcquisition(),
            command.dateMiseEnService());
    return vehiculeRepository.sauvegarder(vehicule).id();
  }

  @Transactional
  public void relever(UUID id, int kilometrage, int heuresMoteur) {
    Vehicule vehicule = trouverOuEchouer(id);
    vehicule.relever(kilometrage, heuresMoteur);
    vehiculeRepository.sauvegarder(vehicule);
  }

  @Transactional
  public void changerStatut(UUID id, StatutVehicule statut) {
    Vehicule vehicule = trouverOuEchouer(id);
    vehicule.changerStatut(statut);
    vehiculeRepository.sauvegarder(vehicule);
  }

  @Transactional
  public void sortir(
      UUID id, LocalDate dateSortie, String motifSortie, Integer kilometrageSortie, Integer heuresMoteurSortie) {
    Vehicule vehicule = trouverOuEchouer(id);
    vehicule.sortir(dateSortie, motifSortie, kilometrageSortie, heuresMoteurSortie);
    vehiculeRepository.sauvegarder(vehicule);
  }

  @Transactional(readOnly = true)
  public Vehicule consulterVehicule(UUID id) {
    return trouverOuEchouer(id);
  }

  @Transactional(readOnly = true)
  public Page<Vehicule> listerVehicules(String texteRecherche, PageRequest pageRequest) {
    return vehiculeRepository.rechercher(texteRecherche, pageRequest);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<VehiculeSummary> consulter(UUID vehiculeId) {
    return vehiculeRepository.parId(vehiculeId).map(this::versResume);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean estDisponible(UUID vehiculeId) {
    return vehiculeRepository.parId(vehiculeId).map(Vehicule::estDisponible).orElse(false);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean documentsValides(UUID vehiculeId, LocalDate date) {
    return vehiculeRepository.parId(vehiculeId).isPresent()
        && documentApi.tousValides(TYPE_ENTITE_DOCUMENTABLE, vehiculeId, date);
  }

  private VehiculeSummary versResume(Vehicule vehicule) {
    return new VehiculeSummary(
        vehicule.id(),
        vehicule.immatriculation().valeur(),
        vehicule.type().name(),
        vehicule.ptac().kg(),
        vehicule.chargeUtile().kg(),
        vehicule.statut().name());
  }

  private Vehicule trouverOuEchouer(UUID id) {
    return vehiculeRepository
        .parId(id)
        .orElseThrow(() -> new NotFoundException("Aucun véhicule trouvé pour l'identifiant " + id));
  }
}
