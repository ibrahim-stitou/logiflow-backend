package com.logiflow.tms.driver.application;

import com.logiflow.tms.document.api.DocumentApi;
import com.logiflow.tms.driver.api.ChauffeurApi;
import com.logiflow.tms.driver.api.dto.ChauffeurSummary;
import com.logiflow.tms.driver.application.command.CreerChauffeurCommand;
import com.logiflow.tms.driver.application.command.MajChauffeurCommand;
import com.logiflow.tms.driver.domain.model.Chauffeur;
import com.logiflow.tms.driver.domain.model.DisponibiliteChauffeur;
import com.logiflow.tms.driver.domain.model.StatutChauffeur;
import com.logiflow.tms.driver.domain.port.out.ChauffeurRepository;
import com.logiflow.tms.driver.domain.service.DriverDomainService;
import com.logiflow.tms.driver.domain.vo.Habilitation.TypeHabilitation;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cas d'utilisation applicatifs du sous-domaine Chauffeur. */
@Service
@RequiredArgsConstructor
public class ChauffeurService implements ChauffeurApi {

  // Doit correspondre à TypeEntiteDocumentable.CHAUFFEUR du module document (contrat en String pour
  // ne pas exposer ce type de domaine hors de son module).
  private static final String TYPE_ENTITE_DOCUMENTABLE = "CHAUFFEUR";

  private final ChauffeurRepository chauffeurRepository;
  private final DriverDomainService driverDomainService;
  private final DocumentApi documentApi;

  @Transactional
  public UUID creerChauffeur(CreerChauffeurCommand command) {
    driverDomainService.verifierMatriculeDisponible(
        command.matricule(), chauffeurRepository.existeParMatricule(command.matricule()));
    Chauffeur chauffeur =
        Chauffeur.creer(
            UUID.randomUUID(),
            command.matricule(),
            command.nom(),
            command.prenom(),
            command.cin(),
            command.dateNaissance(),
            command.lieuNaissance(),
            command.nationalite(),
            command.telephone(),
            command.email(),
            command.adresse(),
            command.numeroPermis(),
            command.categoriePermis(),
            command.dateObtentionPermis(),
            command.dateExpirationPermis(),
            command.numeroPasseport(),
            command.dateDelivrancePasseport(),
            command.dateExpirationPasseport(),
            command.paysDelivrancePasseport(),
            command.numeroVisa(),
            command.typeVisa(),
            command.paysVisa(),
            command.dateDelivranceVisa(),
            command.dateExpirationVisa(),
            command.dateEmbauche(),
            command.typeContrat(),
            command.experienceAnnees(),
            command.specialisation(),
            command.habilitations(),
            Duration.ofMinutes(command.soldeTempsConduiteInitialMinutes()));
    return chauffeurRepository.sauvegarder(chauffeur).id();
  }

  @Transactional
  public void modifierChauffeur(UUID id, MajChauffeurCommand command) {
    Chauffeur chauffeur = trouverOuEchouer(id);
    chauffeur.renommer(command.nom(), command.prenom());
    chauffeur.mettreAJourHabilitations(command.habilitations());
    chauffeurRepository.sauvegarder(chauffeur);
  }

  @Transactional
  public void changerStatut(UUID id, StatutChauffeur statut) {
    Chauffeur chauffeur = trouverOuEchouer(id);
    chauffeur.changerStatut(statut);
    chauffeurRepository.sauvegarder(chauffeur);
  }

  @Transactional
  public void changerDisponibilite(UUID id, DisponibiliteChauffeur disponibilite) {
    Chauffeur chauffeur = trouverOuEchouer(id);
    chauffeur.changerDisponibilite(disponibilite);
    chauffeurRepository.sauvegarder(chauffeur);
  }

  @Transactional(readOnly = true)
  public Chauffeur consulterChauffeur(UUID id) {
    return trouverOuEchouer(id);
  }

  @Transactional(readOnly = true)
  public Page<Chauffeur> listerChauffeurs(String texteRecherche, PageRequest pageRequest) {
    return chauffeurRepository.rechercher(texteRecherche, pageRequest);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ChauffeurSummary> consulter(UUID chauffeurId) {
    return chauffeurRepository.parId(chauffeurId).map(this::versResume);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean estDisponible(UUID chauffeurId) {
    return chauffeurRepository.parId(chauffeurId).map(Chauffeur::estDisponible).orElse(false);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean possedeHabilitationAdr(UUID chauffeurId, LocalDate date) {
    return chauffeurRepository
        .parId(chauffeurId)
        .map(chauffeur -> chauffeur.possedeHabilitation(TypeHabilitation.ADR_BASE, date))
        .orElse(false);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean documentsValides(UUID chauffeurId, LocalDate date) {
    return chauffeurRepository.parId(chauffeurId).isPresent()
        && documentApi.tousValides(TYPE_ENTITE_DOCUMENTABLE, chauffeurId, date);
  }

  private ChauffeurSummary versResume(Chauffeur chauffeur) {
    return new ChauffeurSummary(
        chauffeur.id(),
        chauffeur.matricule(),
        chauffeur.nom(),
        chauffeur.prenom(),
        chauffeur.statut().name(),
        chauffeur.disponibilite().name(),
        chauffeur.soldeTempsConduite().toMinutes());
  }

  private Chauffeur trouverOuEchouer(UUID id) {
    return chauffeurRepository
        .parId(id)
        .orElseThrow(
            () -> new NotFoundException("Aucun chauffeur trouvé pour l'identifiant " + id));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ChauffeurSummary> rechercher(
      String texte, String disponibilite, PageRequest pageRequest) {
    return chauffeurRepository
        .rechercherParDisponibilite(texte, disponibilite, pageRequest)
        .map(this::versResume);
  }

  @Override
  public List<String> disponibilitesConnues() {
    return Arrays.stream(DisponibiliteChauffeur.values()).map(Enum::name).toList();
  }
}
