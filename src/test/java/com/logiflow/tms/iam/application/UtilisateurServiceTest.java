package com.logiflow.tms.iam.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.logiflow.tms.iam.application.command.CreerUtilisateurCommand;
import com.logiflow.tms.iam.domain.model.RoleUtilisateur;
import com.logiflow.tms.iam.domain.model.Utilisateur;
import com.logiflow.tms.iam.domain.port.out.UtilisateurRepository;
import com.logiflow.tms.iam.domain.service.IamDomainService;
import com.logiflow.tms.shared.domain.exception.ConflictException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceTest {

  @Mock private UtilisateurRepository utilisateurRepository;

  private UtilisateurService utilisateurService;

  @BeforeEach
  void setUp() {
    utilisateurService = new UtilisateurService(utilisateurRepository, new IamDomainService());
  }

  @Test
  void creerUtilisateurEchoueSiLeLoginEstDejaUtilise() {
    CreerUtilisateurCommand command =
        new CreerUtilisateurCommand(
            "jean.dupont", "jean@logiflow.tms", Set.of(RoleUtilisateur.EXPLOITANT));
    when(utilisateurRepository.existeParLogin("jean.dupont")).thenReturn(true);

    assertThatThrownBy(() -> utilisateurService.creerUtilisateur(command))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void creerUtilisateurSauvegardeUnNouvelUtilisateur() {
    CreerUtilisateurCommand command =
        new CreerUtilisateurCommand(
            "jean.dupont", "jean@logiflow.tms", Set.of(RoleUtilisateur.EXPLOITANT));
    when(utilisateurRepository.existeParLogin("jean.dupont")).thenReturn(false);
    when(utilisateurRepository.sauvegarder(any(Utilisateur.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UUID id = utilisateurService.creerUtilisateur(command);

    assertThat(id).isNotNull();
  }

  @Test
  void consulterParLoginRenvoieVideSiIntrouvable() {
    when(utilisateurRepository.parLogin("inconnu")).thenReturn(Optional.empty());

    assertThat(utilisateurService.consulterParLogin("inconnu")).isEmpty();
  }
}
