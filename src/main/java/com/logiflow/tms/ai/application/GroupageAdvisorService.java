package com.logiflow.tms.ai.application;

import com.logiflow.tms.ai.application.command.AnalyserGroupageCommand;
import com.logiflow.tms.ai.domain.model.CandidatDossier;
import com.logiflow.tms.ai.domain.model.InteractionIa;
import com.logiflow.tms.ai.domain.model.PropositionGroupage;
import com.logiflow.tms.ai.domain.model.TypeInteractionIa;
import com.logiflow.tms.ai.domain.port.out.AiServiceClientPort;
import com.logiflow.tms.ai.domain.port.out.InteractionIaRepository;
import com.logiflow.tms.ai.domain.service.AiDomainService;
import com.logiflow.tms.dossier.api.DossierApi;
import com.logiflow.tms.dossier.api.dto.DossierSummary;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import com.logiflow.tms.shared.domain.exception.ServiceIndisponibleException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cas d'utilisation applicatif de l'agent de groupage. Assemble le contexte à partir de {@code
 * dossier.api.DossierApi} (seul point d'entrée autorisé vers le module {@code dossier}), interroge
 * le service IA, et se replie sur {@link AiDomainService#repliSansIa} si celui-ci est indisponible
 * — le flux métier de groupage n'est jamais bloqué par une panne de l'IA.
 */
@Service
@RequiredArgsConstructor
public class GroupageAdvisorService {

  private final AiServiceClientPort aiServiceClientPort;
  private final InteractionIaRepository interactionRepository;
  private final AiDomainService aiDomainService;
  private final DossierApi dossierApi;

  @Transactional
  public List<PropositionGroupage> analyserGroupage(AnalyserGroupageCommand command) {
    List<CandidatDossier> candidats =
        command.dossierIds().stream().map(this::resoudreCandidat).toList();
    String resume =
        "%d dossier(s) analysé(s) : %s".formatted(candidats.size(), command.dossierIds());
    Instant debut = Instant.now();

    try {
      List<PropositionGroupage> propositions = aiServiceClientPort.analyserGroupage(candidats);
      journaliser(true, debut, resume, null);
      return propositions;
    } catch (ServiceIndisponibleException e) {
      journaliser(false, debut, resume, e.getMessage());
      return aiDomainService.repliSansIa(candidats);
    }
  }

  private CandidatDossier resoudreCandidat(UUID dossierId) {
    DossierSummary dossier =
        dossierApi
            .consulter(dossierId)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Aucun dossier de transport trouvé pour l'identifiant " + dossierId));
    return new CandidatDossier(
        dossier.id(),
        dossier.reference(),
        dossier.poidsBrutKg(),
        dossier.volumeM3(),
        dossier.nbPalettes(),
        dossier.contientAdr(),
        dossier.groupable());
  }

  private void journaliser(boolean succes, Instant debut, String resume, String erreur) {
    long dureeMs = Duration.between(debut, Instant.now()).toMillis();
    interactionRepository.sauvegarder(
        InteractionIa.enregistrer(
            UUID.randomUUID(), TypeInteractionIa.GROUPAGE, null, succes, dureeMs, resume, erreur));
  }
}
