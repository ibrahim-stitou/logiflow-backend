package com.logiflow.tms.driver.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.logiflow.tms.driver.domain.model.Chauffeur;
import com.logiflow.tms.driver.domain.model.StatutChauffeur;
import com.logiflow.tms.driver.domain.vo.Habilitation;
import com.logiflow.tms.driver.domain.vo.Habilitation.TypeHabilitation;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChauffeurTest {

  @Test
  void creerUnChauffeurEstDisponibleParDefaut() {
    Chauffeur chauffeur =
        Chauffeur.creer(
            UUID.randomUUID(), "CH-001", "Jean Dupont", List.of(), Duration.ofHours(35));

    assertThat(chauffeur.estDisponible()).isTrue();
    assertThat(chauffeur.matricule()).isEqualTo("CH-001");
  }

  @Test
  void consommerPlusDeTempsQueLeSoldeDisponibleEchoue() {
    Chauffeur chauffeur =
        Chauffeur.creer(UUID.randomUUID(), "CH-001", "Jean Dupont", List.of(), Duration.ofHours(2));

    assertThatThrownBy(() -> chauffeur.consommerTempsConduite(Duration.ofHours(3)))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void consommerPuisCrediterAjusteLeSolde() {
    Chauffeur chauffeur =
        Chauffeur.creer(
            UUID.randomUUID(), "CH-001", "Jean Dupont", List.of(), Duration.ofHours(10));

    chauffeur.consommerTempsConduite(Duration.ofHours(4));
    assertThat(chauffeur.soldeTempsConduite()).isEqualTo(Duration.ofHours(6));

    chauffeur.crediterTempsConduite(Duration.ofHours(1));
    assertThat(chauffeur.soldeTempsConduite()).isEqualTo(Duration.ofHours(7));
  }

  @Test
  void possedeHabilitationAdrValideALaDateDonnee() {
    Habilitation adr =
        new Habilitation(
            TypeHabilitation.ADR_BASE,
            "ADR-001",
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2027, 1, 1));
    Chauffeur chauffeur =
        Chauffeur.creer(
            UUID.randomUUID(), "CH-001", "Jean Dupont", List.of(adr), Duration.ofHours(10));

    assertThat(chauffeur.possedeHabilitation(TypeHabilitation.ADR_BASE, LocalDate.of(2026, 1, 1)))
        .isTrue();
    assertThat(chauffeur.possedeHabilitation(TypeHabilitation.ADR_BASE, LocalDate.of(2028, 1, 1)))
        .isFalse();
    assertThat(
            chauffeur.possedeHabilitation(TypeHabilitation.ADR_CITERNE, LocalDate.of(2026, 1, 1)))
        .isFalse();
  }

  @Test
  void changerStatutMetAJourLaDisponibilite() {
    Chauffeur chauffeur =
        Chauffeur.creer(UUID.randomUUID(), "CH-001", "Jean Dupont", List.of(), Duration.ZERO);

    chauffeur.changerStatut(StatutChauffeur.EN_CONGE);

    assertThat(chauffeur.estDisponible()).isFalse();
  }
}
