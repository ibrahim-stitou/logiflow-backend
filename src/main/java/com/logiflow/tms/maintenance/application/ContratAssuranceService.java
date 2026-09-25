package com.logiflow.tms.maintenance.application;

import com.logiflow.tms.maintenance.domain.model.ContratAssurance;
import com.logiflow.tms.maintenance.domain.model.TypePrestataire;
import com.logiflow.tms.maintenance.domain.port.out.ContratAssuranceRepository;
import com.logiflow.tms.maintenance.domain.vo.EnginRef;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gestion des contrats d'assurance de la flotte. */
@Service
@RequiredArgsConstructor
public class ContratAssuranceService {

  private final ContratAssuranceRepository repository;
  private final PrestataireService prestataireService;
  private final EnginsFlotte enginsFlotte;

  @Transactional
  public ContratAssurance creer(ContratAssurance.Conditions conditions) {
    verifier(conditions);
    return repository.sauvegarder(ContratAssurance.creer(UUID.randomUUID(), conditions));
  }

  @Transactional
  public ContratAssurance modifier(UUID id, ContratAssurance.Conditions conditions) {
    verifier(conditions);
    ContratAssurance contrat = consulter(id);
    contrat.modifier(conditions);
    return repository.sauvegarder(contrat);
  }

  @Transactional(readOnly = true)
  public ContratAssurance consulter(UUID id) {
    return repository
        .parId(id)
        .orElseThrow(
            () ->
                new NotFoundException("Aucun contrat d'assurance trouvé pour l'identifiant " + id));
  }

  @Transactional(readOnly = true)
  public Page<ContratAssurance> rechercher(Boolean actif, PageRequest pageRequest) {
    return repository.rechercher(actif, pageRequest);
  }

  /** Contrat qui couvre l'engin à la date, un contrat dédié primant sur le contrat de flotte. */
  @Transactional(readOnly = true)
  public Optional<ContratAssurance> applicable(EnginRef engin, LocalDate date) {
    return repository.actifs().stream()
        .filter(c -> c.couvre(engin, date))
        .max(Comparator.comparingInt(ContratAssurance::specificite));
  }

  private void verifier(ContratAssurance.Conditions conditions) {
    prestataireService.verifier(conditions.assureurId(), TypePrestataire.ASSUREUR);
    conditions.engins().forEach(enginsFlotte::verifierExiste);
  }
}
