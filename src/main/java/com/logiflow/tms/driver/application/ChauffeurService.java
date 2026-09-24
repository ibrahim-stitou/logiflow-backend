package com.logiflow.tms.driver.application;

import com.logiflow.tms.document.api.DocumentApi;
import com.logiflow.tms.driver.api.ChauffeurApi;
import com.logiflow.tms.driver.api.dto.ChauffeurPlanificationSummary;
import com.logiflow.tms.driver.api.dto.ChauffeurSummary;
import com.logiflow.tms.driver.api.dto.ExigencesAffectationDto;
import com.logiflow.tms.driver.application.command.CreerChauffeurCommand;
import com.logiflow.tms.driver.application.command.MajChauffeurCommand;
import com.logiflow.tms.driver.domain.model.CategoriePermis;
import com.logiflow.tms.driver.domain.model.Chauffeur;
import com.logiflow.tms.driver.domain.model.DisponibiliteChauffeur;
import com.logiflow.tms.driver.domain.model.ExigencesAffectation;
import com.logiflow.tms.driver.domain.model.StatutChauffeur;
import com.logiflow.tms.driver.domain.port.out.ChauffeurRepository;
import com.logiflow.tms.driver.domain.service.DriverDomainService;
import com.logiflow.tms.driver.domain.vo.Habilitation.TypeHabilitation;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
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

  private static final int PAGE_PLANIFICATION = 100;

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
            command.profil(),
            command.habilitations(),
            Duration.ofMinutes(command.soldeTempsConduiteInitialMinutes()));
    return chauffeurRepository.sauvegarder(chauffeur).id();
  }

  @Transactional
  public void modifierChauffeur(UUID id, MajChauffeurCommand command) {
    Chauffeur chauffeur = trouverOuEchouer(id);
    chauffeur.renommer(command.nom(), command.prenom());
    chauffeur.mettreAJourProfil(command.profil());
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

  /** Liste filtrée (texte, statut et disponibilité optionnels), pour l'écran de gestion. */
  @Transactional(readOnly = true)
  public Page<Chauffeur> listerChauffeurs(
      String texteRecherche,
      StatutChauffeur statut,
      DisponibiliteChauffeur disponibilite,
      PageRequest pageRequest) {
    return chauffeurRepository.rechercherFiltre(
        texteRecherche,
        statut != null ? statut.name() : null,
        disponibilite != null ? disponibilite.name() : null,
        pageRequest);
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

  @Override
  @Transactional(readOnly = true)
  public List<String> motifsNonAffectation(UUID chauffeurId, ExigencesAffectationDto exigences) {
    return chauffeurRepository
        .parId(chauffeurId)
        .map(chauffeur -> chauffeur.motifsNonAffectation(versExigences(exigences)))
        .orElseGet(() -> List.of("Chauffeur introuvable : " + chauffeurId));
  }

  @Override
  @Transactional(readOnly = true)
  public List<ChauffeurPlanificationSummary> listerPourPlanification(LocalDate date) {
    List<ChauffeurPlanificationSummary> resultat = new ArrayList<>();
    PageRequest page = PageRequest.premiere(PAGE_PLANIFICATION);
    Page<Chauffeur> courante;
    do {
      courante =
          chauffeurRepository.rechercherFiltre(null, StatutChauffeur.ACTIF.name(), null, page);
      courante.contenu().stream().map(c -> versPlanification(c, date)).forEach(resultat::add);
      page = new PageRequest(page.numero() + 1, PAGE_PLANIFICATION);
    } while (page.numero() < courante.totalPages());
    return resultat;
  }

  private ChauffeurPlanificationSummary versPlanification(Chauffeur chauffeur, LocalDate date) {
    var profil = chauffeur.profil();
    boolean passeportValide =
        profil.numeroPasseport() != null
            && profil.dateExpirationPasseport() != null
            && !profil.dateExpirationPasseport().isBefore(date);
    return new ChauffeurPlanificationSummary(
        chauffeur.id(),
        chauffeur.matricule(),
        chauffeur.nom(),
        chauffeur.prenom(),
        chauffeur.statut().name(),
        chauffeur.disponibilite().name(),
        chauffeur.soldeTempsConduite().toMinutes(),
        profil.categoriesPermis().stream().map(Enum::name).sorted().toList(),
        chauffeur.habilitations().stream()
            .filter(h -> h.estValide(date))
            .map(h -> h.type().name())
            .distinct()
            .toList(),
        passeportValide,
        profil.siteRattachementId(),
        chauffeur.motifsNonAffectation(new ExigencesAffectation(date, false, false, null)));
  }

  private static ExigencesAffectation versExigences(ExigencesAffectationDto dto) {
    CategoriePermis permis =
        dto.permisRequis() == null || dto.permisRequis().isBlank()
            ? null
            : CategoriePermis.valueOf(dto.permisRequis());
    return new ExigencesAffectation(dto.date(), dto.adr(), dto.international(), permis);
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
