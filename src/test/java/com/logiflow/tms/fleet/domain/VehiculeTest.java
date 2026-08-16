package com.logiflow.tms.fleet.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.logiflow.tms.fleet.domain.model.StatutVehicule;
import com.logiflow.tms.fleet.domain.model.TypeVehicule;
import com.logiflow.tms.fleet.domain.model.Vehicule;
import com.logiflow.tms.fleet.domain.vo.DocumentVehicule;
import com.logiflow.tms.fleet.domain.vo.DocumentVehicule.TypeDocumentVehicule;
import com.logiflow.tms.shared.domain.vo.Immatriculation;
import com.logiflow.tms.shared.domain.vo.Poids;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class VehiculeTest {

  private static final Immatriculation IMMAT = new Immatriculation("AB-123-CD");

  @Test
  void creerUnVehiculeEstDisponibleParDefaut() {
    Vehicule vehicule =
        Vehicule.creer(
            UUID.randomUUID(),
            IMMAT,
            TypeVehicule.PORTEUR,
            new Poids(19000),
            new Poids(9000),
            List.of());

    assertThat(vehicule.estDisponible()).isTrue();
    assertThat(vehicule.statut()).isEqualTo(StatutVehicule.DISPONIBLE);
    assertThat(vehicule.kilometrage()).isZero();
  }

  @Test
  void relerverUnKilometrageInferieurEchoue() {
    Vehicule vehicule =
        Vehicule.creer(
            UUID.randomUUID(),
            IMMAT,
            TypeVehicule.PORTEUR,
            new Poids(19000),
            new Poids(9000),
            List.of());
    vehicule.relever(1000, 50);

    assertThatThrownBy(() -> vehicule.relever(500, 60))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void changerStatutMetAJourLaDisponibilite() {
    Vehicule vehicule =
        Vehicule.creer(
            UUID.randomUUID(),
            IMMAT,
            TypeVehicule.TRACTEUR,
            new Poids(19000),
            new Poids(9000),
            List.of());

    vehicule.changerStatut(StatutVehicule.EN_MAINTENANCE);

    assertThat(vehicule.estDisponible()).isFalse();
  }

  @Test
  void unDocumentExpireRendLeVehiculeNonConforme() {
    DocumentVehicule assuranceExpiree =
        new DocumentVehicule(TypeDocumentVehicule.ASSURANCE, "ASS-001", LocalDate.of(2020, 1, 1));
    Vehicule vehicule =
        Vehicule.creer(
            UUID.randomUUID(),
            IMMAT,
            TypeVehicule.PORTEUR,
            new Poids(19000),
            new Poids(9000),
            List.of(assuranceExpiree));

    assertThat(vehicule.documentsValides(LocalDate.of(2026, 1, 1))).isFalse();
  }

  @Test
  void sansDocumentExpireLeVehiculeEstConforme() {
    DocumentVehicule assuranceValide =
        new DocumentVehicule(TypeDocumentVehicule.ASSURANCE, "ASS-002", LocalDate.of(2030, 1, 1));
    Vehicule vehicule =
        Vehicule.creer(
            UUID.randomUUID(),
            IMMAT,
            TypeVehicule.PORTEUR,
            new Poids(19000),
            new Poids(9000),
            List.of(assuranceValide));

    assertThat(vehicule.documentsValides(LocalDate.of(2026, 1, 1))).isTrue();
  }
}
