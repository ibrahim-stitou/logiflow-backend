package com.logiflow.tms.tracking.application;

import com.logiflow.tms.planning.api.VoyageApi;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import com.logiflow.tms.tracking.api.TrackingApi;
import com.logiflow.tms.tracking.api.dto.EvenementVoyageSummary;
import com.logiflow.tms.tracking.application.command.DeclarerEvenementCommand;
import com.logiflow.tms.tracking.domain.model.EvenementVoyage;
import com.logiflow.tms.tracking.domain.port.out.EvenementVoyageRepository;
import com.logiflow.tms.tracking.domain.service.TrackingDomainService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cas d'utilisation applicatifs du sous-domaine Suivi d'exécution. */
@Service
@RequiredArgsConstructor
public class EvenementVoyageService implements TrackingApi {

  private final EvenementVoyageRepository evenementRepository;
  private final TrackingDomainService trackingDomainService;
  private final VoyageApi voyageApi;

  @Transactional
  public UUID declarerEvenement(DeclarerEvenementCommand command) {
    if (voyageApi.consulter(command.voyageId()).isEmpty()) {
      throw new NotFoundException("Aucun voyage trouvé pour l'identifiant " + command.voyageId());
    }
    Optional<EvenementVoyage> dernier = evenementRepository.dernierParVoyageId(command.voyageId());
    trackingDomainService.verifierChronologie(
        command.horodatage(), dernier.map(EvenementVoyage::horodatage));

    EvenementVoyage evenement =
        EvenementVoyage.declarer(
            UUID.randomUUID(),
            command.voyageId(),
            command.type(),
            command.horodatage(),
            command.position(),
            command.commentaire());
    return evenementRepository.sauvegarder(evenement).id();
  }

  @Transactional(readOnly = true)
  public EvenementVoyage consulterEvenement(UUID id) {
    return evenementRepository
        .parId(id)
        .orElseThrow(
            () -> new NotFoundException("Aucun événement trouvé pour l'identifiant " + id));
  }

  @Transactional(readOnly = true)
  public List<EvenementVoyage> listerParVoyage(UUID voyageId) {
    return evenementRepository.parVoyageId(voyageId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<EvenementVoyageSummary> dernierEvenement(UUID voyageId) {
    return evenementRepository
        .dernierParVoyageId(voyageId)
        .map(
            e ->
                new EvenementVoyageSummary(
                    e.voyageId(),
                    e.type().name(),
                    e.horodatage(),
                    e.position() != null ? e.position().latitude() : null,
                    e.position() != null ? e.position().longitude() : null));
  }
}
