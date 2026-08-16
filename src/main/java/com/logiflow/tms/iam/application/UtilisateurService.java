package com.logiflow.tms.iam.application;

import com.logiflow.tms.iam.api.UtilisateurApi;
import com.logiflow.tms.iam.api.dto.UtilisateurSummary;
import com.logiflow.tms.iam.application.command.CreerUtilisateurCommand;
import com.logiflow.tms.iam.application.command.MajUtilisateurCommand;
import com.logiflow.tms.iam.domain.model.Utilisateur;
import com.logiflow.tms.iam.domain.port.out.UtilisateurRepository;
import com.logiflow.tms.iam.domain.service.IamDomainService;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cas d'utilisation applicatifs du sous-domaine Utilisateur. */
@Service
@RequiredArgsConstructor
public class UtilisateurService implements UtilisateurApi {

  private final UtilisateurRepository utilisateurRepository;
  private final IamDomainService iamDomainService;

  @Transactional
  public UUID creerUtilisateur(CreerUtilisateurCommand command) {
    iamDomainService.verifierLoginDisponible(
        command.login(), utilisateurRepository.existeParLogin(command.login()));
    Utilisateur utilisateur =
        Utilisateur.creer(UUID.randomUUID(), command.login(), command.email(), command.roles());
    return utilisateurRepository.sauvegarder(utilisateur).id();
  }

  @Transactional
  public void modifierUtilisateur(UUID id, MajUtilisateurCommand command) {
    Utilisateur utilisateur = trouverOuEchouer(id);
    utilisateur.changerEmail(command.email());
    utilisateur.changerRoles(command.roles());
    utilisateurRepository.sauvegarder(utilisateur);
  }

  @Transactional
  public void desactiverUtilisateur(UUID id) {
    Utilisateur utilisateur = trouverOuEchouer(id);
    utilisateur.desactiver();
    utilisateurRepository.sauvegarder(utilisateur);
  }

  @Transactional(readOnly = true)
  public Utilisateur consulterUtilisateur(UUID id) {
    return trouverOuEchouer(id);
  }

  @Transactional(readOnly = true)
  public Page<Utilisateur> listerUtilisateurs(String texteRecherche, PageRequest pageRequest) {
    return utilisateurRepository.rechercher(texteRecherche, pageRequest);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UtilisateurSummary> consulter(UUID utilisateurId) {
    return utilisateurRepository.parId(utilisateurId).map(this::versResume);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UtilisateurSummary> consulterParLogin(String login) {
    return utilisateurRepository.parLogin(login).map(this::versResume);
  }

  private UtilisateurSummary versResume(Utilisateur utilisateur) {
    return new UtilisateurSummary(
        utilisateur.id(),
        utilisateur.login(),
        utilisateur.email(),
        utilisateur.roles().stream().map(Enum::name).collect(Collectors.toUnmodifiableSet()),
        utilisateur.estActif());
  }

  private Utilisateur trouverOuEchouer(UUID id) {
    return utilisateurRepository
        .parId(id)
        .orElseThrow(
            () -> new NotFoundException("Aucun utilisateur trouvé pour l'identifiant " + id));
  }
}
