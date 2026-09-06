package com.logiflow.tms.driver.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.logiflow.tms.document.api.DocumentApi;
import com.logiflow.tms.driver.application.command.CreerChauffeurCommand;
import com.logiflow.tms.driver.domain.model.Chauffeur;
import com.logiflow.tms.driver.domain.port.out.ChauffeurRepository;
import com.logiflow.tms.driver.domain.service.DriverDomainService;
import com.logiflow.tms.shared.domain.exception.ConflictException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChauffeurServiceTest {

  @Mock private ChauffeurRepository chauffeurRepository;
  @Mock private DocumentApi documentApi;

  private ChauffeurService chauffeurService;

  @BeforeEach
  void setUp() {
    chauffeurService =
        new ChauffeurService(chauffeurRepository, new DriverDomainService(), documentApi);
  }

  private static CreerChauffeurCommand commandeChauffeur() {
    return new CreerChauffeurCommand(
        "CH-001", "Dupont", "Jean", null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        List.of(), 2100);
  }

  @Test
  void creerChauffeurEchoueSiLeMatriculeEstDejaUtilise() {
    CreerChauffeurCommand command = commandeChauffeur();
    when(chauffeurRepository.existeParMatricule("CH-001")).thenReturn(true);

    assertThatThrownBy(() -> chauffeurService.creerChauffeur(command))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void creerChauffeurSauvegardeUnNouveauChauffeur() {
    CreerChauffeurCommand command = commandeChauffeur();
    when(chauffeurRepository.existeParMatricule("CH-001")).thenReturn(false);
    when(chauffeurRepository.sauvegarder(any(Chauffeur.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UUID id = chauffeurService.creerChauffeur(command);

    assertThat(id).isNotNull();
  }

  @Test
  void estDisponibleRenvoieFauxSiLeChauffeurEstIntrouvable() {
    UUID id = UUID.randomUUID();
    when(chauffeurRepository.parId(id)).thenReturn(Optional.empty());

    assertThat(chauffeurService.estDisponible(id)).isFalse();
  }
}
