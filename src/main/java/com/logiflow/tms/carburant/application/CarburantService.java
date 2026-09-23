package com.logiflow.tms.carburant.application;

import com.logiflow.tms.carburant.api.CarburantApi;
import com.logiflow.tms.carburant.application.command.CreerPriseCarburantCommand;
import com.logiflow.tms.carburant.application.command.CreerStationCommand;
import com.logiflow.tms.carburant.application.command.MajPriseCarburantCommand;
import com.logiflow.tms.carburant.application.command.MajStationCommand;
import com.logiflow.tms.carburant.domain.model.PriseCarburant;
import com.logiflow.tms.carburant.domain.model.Station;
import com.logiflow.tms.carburant.domain.model.StatutPrise;
import com.logiflow.tms.carburant.domain.port.out.PriseCarburantRepository;
import com.logiflow.tms.carburant.domain.port.out.StationRepository;
import com.logiflow.tms.carburant.domain.service.StationDomainService;
import com.logiflow.tms.planning.api.VoyageApi;
import com.logiflow.tms.planning.api.dto.VoyageSummary;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CarburantService implements CarburantApi {

  private final StationRepository stationRepository;
  private final PriseCarburantRepository priseCarburantRepository;
  private final StationDomainService stationDomainService;
  private final VoyageApi voyageApi;

  @Transactional
  public UUID creerStation(CreerStationCommand command) {
    stationDomainService.verifierCodeDisponible(
        command.code(), stationRepository.existeParCode(command.code()));
    Station station =
        Station.creer(
            UUID.randomUUID(), command.code(), command.libelle(), command.adresse());
    return stationRepository.sauvegarder(station).id();
  }

  @Transactional
  public void modifierStation(UUID id, MajStationCommand command) {
    Station station = trouverStationOuEchouer(id);
    station.modifier(command.libelle(), command.adresse());
    stationRepository.sauvegarder(station);
  }

  @Transactional
  public void desactiverStation(UUID id) {
    Station station = trouverStationOuEchouer(id);
    station.desactiver();
    stationRepository.sauvegarder(station);
  }

  @Transactional(readOnly = true)
  public Station consulterStation(UUID id) {
    return trouverStationOuEchouer(id);
  }

  @Transactional(readOnly = true)
  public Page<Station> listerStations(String texteRecherche, PageRequest pageRequest) {
    return stationRepository.rechercher(texteRecherche, pageRequest);
  }

  @Transactional
  public UUID creerPriseCarburant(CreerPriseCarburantCommand command) {
    VoyageSummary voyage = trouverVoyageOuEchouer(command.voyageId());
    verifierEnginVoyage(voyage, command.vehiculeId(), command.remorqueId());
    Station station = trouverStationOuEchouer(command.stationId());
    if (!station.estActif()) {
      throw new BusinessException("La station sélectionnée est inactive");
    }
    PriseCarburant prise =
        PriseCarburant.creer(
            UUID.randomUUID(),
            command.voyageId(),
            command.vehiculeId(),
            command.remorqueId(),
            command.stationId(),
            command.typeCarburant(),
            command.litrage(),
            command.montantTtc(),
            command.datePrise());
    return priseCarburantRepository.sauvegarder(prise).id();
  }

  @Transactional
  public void modifierPriseCarburant(UUID id, MajPriseCarburantCommand command) {
    PriseCarburant prise = trouverPriseOuEchouer(id);
    Station station = trouverStationOuEchouer(command.stationId());
    if (!station.estActif()) {
      throw new BusinessException("La station sélectionnée est inactive");
    }
    prise.modifier(
        command.stationId(), command.typeCarburant(), command.litrage(), command.montantTtc());
    priseCarburantRepository.sauvegarder(prise);
  }

  @Transactional
  public void validerPriseCarburant(UUID id) {
    PriseCarburant prise = trouverPriseOuEchouer(id);
    prise.valider();
    priseCarburantRepository.sauvegarder(prise);
  }

  @Transactional(readOnly = true)
  public PriseCarburant consulterPriseCarburant(UUID id) {
    return trouverPriseOuEchouer(id);
  }

  @Transactional(readOnly = true)
  public Page<PriseCarburant> listerPrisesCarburant(
      String texteRecherche, UUID voyageId, StatutPrise statut, PageRequest pageRequest) {
    return priseCarburantRepository.rechercher(texteRecherche, voyageId, statut, pageRequest);
  }

  @Transactional(readOnly = true)
  public PriseCarburantRepository.PriseCarburantStats statsPrisesCarburant(
      String texteRecherche, UUID voyageId, StatutPrise statut) {
    return priseCarburantRepository.stats(texteRecherche, voyageId, statut);
  }

  @Override
  @Transactional(readOnly = true)
  public void verifierPriseModifiable(UUID priseId) {
    PriseCarburant prise = trouverPriseOuEchouer(priseId);
    if (!prise.estModifiable()) {
      throw new BusinessException(
          "Les documents d'une prise validée ne peuvent plus être modifiés");
    }
  }

  private VoyageSummary trouverVoyageOuEchouer(UUID voyageId) {
    return voyageApi
        .consulter(voyageId)
        .orElseThrow(
            () -> new NotFoundException("Aucun voyage trouvé pour l'identifiant " + voyageId));
  }

  private void verifierEnginVoyage(VoyageSummary voyage, UUID vehiculeId, UUID remorqueId) {
    if (vehiculeId != null && !vehiculeId.equals(voyage.vehiculeId())) {
      throw new BusinessException("Le véhicule doit être celui affecté au voyage");
    }
    if (remorqueId != null) {
      if (voyage.remorqueId() == null) {
        throw new BusinessException("Ce voyage n'a pas de remorque affectée");
      }
      if (!remorqueId.equals(voyage.remorqueId())) {
        throw new BusinessException("La remorque doit être celle affectée au voyage");
      }
    }
  }

  private Station trouverStationOuEchouer(UUID id) {
    return stationRepository
        .parId(id)
        .orElseThrow(
            () -> new NotFoundException("Aucune station trouvée pour l'identifiant " + id));
  }

  private PriseCarburant trouverPriseOuEchouer(UUID id) {
    return priseCarburantRepository
        .parId(id)
        .orElseThrow(
            () -> new NotFoundException("Aucune prise de carburant trouvée pour l'identifiant " + id));
  }
}
