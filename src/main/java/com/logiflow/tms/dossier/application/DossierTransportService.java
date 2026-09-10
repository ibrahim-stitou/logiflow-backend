package com.logiflow.tms.dossier.application;

import com.logiflow.tms.dossier.api.DossierApi;
import com.logiflow.tms.dossier.api.dto.DossierSummary;
import com.logiflow.tms.dossier.application.command.CreerDossierCommand;
import com.logiflow.tms.dossier.domain.model.DossierTransport;
import com.logiflow.tms.dossier.domain.model.StatutDossier;
import com.logiflow.tms.dossier.domain.port.out.DossierTransportRepository;
import com.logiflow.tms.dossier.domain.port.out.SequenceReferenceGenerator;
import com.logiflow.tms.dossier.domain.vo.LigneMarchandise;
import com.logiflow.tms.order.api.CommandeApi;
import com.logiflow.tms.referential.api.MarchandiseApi;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cas d'utilisation applicatifs du sous-domaine Dossier de transport. */
@Service
@RequiredArgsConstructor
public class DossierTransportService implements DossierApi {

  private static final String PREFIXE_REFERENCE = "DT";

  private final DossierTransportRepository dossierRepository;
  private final SequenceReferenceGenerator referenceGenerator;
  private final CommandeApi commandeApi;
  private final MarchandiseApi marchandiseApi;

  @Transactional
  public UUID creerDossier(CreerDossierCommand command) {
    if (!commandeApi.estConfirmee(command.commandeId())) {
      throw new BusinessException(
          "Seule une commande confirmée peut générer un dossier de transport (commande "
              + command.commandeId()
              + ")");
    }
    for (LigneMarchandise ligne : command.lignesMarchandise()) {
      if (!marchandiseApi.estActif(ligne.marchandiseId())) {
        throw new NotFoundException(
            "Aucune marchandise active trouvée pour l'identifiant " + ligne.marchandiseId());
      }
    }

    double poidsBrutKg =
        command.lignesMarchandise().stream().mapToDouble(LigneMarchandise::poidsKg).sum();
    double volumeM3 =
        command.lignesMarchandise().stream().mapToDouble(LigneMarchandise::volumeM3).sum();
    var reference = referenceGenerator.generer(PREFIXE_REFERENCE, Year.now().getValue());

    DossierTransport dossier =
        DossierTransport.creer(
            UUID.randomUUID(),
            reference,
            command.commandeId(),
            command.typeTransport(),
            command.groupable(),
            poidsBrutKg,
            volumeM3,
            command.nbPalettes(),
            command.familleMarchandise(),
            command.carrosserieRequise(),
            command.temperatureRequise(),
            command.lignesMarchandise(),
            command.segments(),
            command.documents());
    return dossierRepository.sauvegarder(dossier).id();
  }

  @Transactional
  public void changerStatut(UUID id, StatutDossier statut) {
    if (statut == StatutDossier.PLANIFIE) {
      throw new BusinessException(
          "Le statut PLANIFIE est réservé à la planification d'un voyage");
    }
    DossierTransport dossier = trouverOuEchouer(id);
    if (dossier.statut() == StatutDossier.PLANIFIE && statut == StatutDossier.CREE) {
      throw new BusinessException(
          "Le retour à CREE depuis PLANIFIE est réservé à l'annulation du voyage");
    }
    dossier.changerStatut(statut);
    dossierRepository.sauvegarder(dossier);
  }

  @Override
  @Transactional
  public void planifierPourVoyage(List<UUID> dossierIds) {
    for (UUID dossierId : dossierIds) {
      DossierTransport dossier = trouverOuEchouer(dossierId);
      if (dossier.statut() != StatutDossier.CREE) {
        throw new BusinessException(
            "Seul un dossier au statut CREE peut être planifié sur un voyage ("
                + dossier.reference().valeur()
                + ")");
      }
      dossier.changerStatut(StatutDossier.PLANIFIE);
      dossierRepository.sauvegarder(dossier);
    }
  }

  @Override
  @Transactional
  public void replanifierApresAnnulationVoyage(List<UUID> dossierIds) {
    for (UUID dossierId : dossierIds) {
      DossierTransport dossier = trouverOuEchouer(dossierId);
      if (dossier.statut() == StatutDossier.PLANIFIE) {
        dossier.changerStatut(StatutDossier.CREE);
        dossierRepository.sauvegarder(dossier);
      }
    }
  }

  @Transactional(readOnly = true)
  public DossierTransport consulterDossier(UUID id) {
    return trouverOuEchouer(id);
  }

  @Transactional(readOnly = true)
  public List<DossierTransport> listerParCommande(UUID commandeId) {
    return dossierRepository.parCommandeId(commandeId);
  }

  @Transactional(readOnly = true)
  public Page<DossierTransport> listerDossiers(PageRequest pageRequest) {
    return dossierRepository.rechercher(pageRequest);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<DossierSummary> consulter(UUID dossierId) {
    return dossierRepository.parId(dossierId).map(this::versResume);
  }

  /** Résout la conformité ADR du dossier via le catalogue référentiel des marchandises. */
  @Transactional(readOnly = true)
  public boolean dossierContientAdr(DossierTransport dossier) {
    return dossier.contientAdr(marchandiseApi::estDangereuse);
  }

  private DossierSummary versResume(DossierTransport dossier) {
    return new DossierSummary(
        dossier.id(),
        dossier.reference().valeur(),
        dossier.statut().name(),
        dossier.groupable(),
        dossier.poidsBrutKg(),
        dossier.volumeM3(),
        dossier.nbPalettes(),
        dossierContientAdr(dossier));
  }

  private DossierTransport trouverOuEchouer(UUID id) {
    return dossierRepository
        .parId(id)
        .orElseThrow(
            () ->
                new NotFoundException(
                    "Aucun dossier de transport trouvé pour l'identifiant " + id));
  }
}
