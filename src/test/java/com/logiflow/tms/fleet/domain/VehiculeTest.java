package com.logiflow.tms.fleet.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.logiflow.tms.fleet.domain.model.StatutVehicule;
import com.logiflow.tms.fleet.domain.model.TypeVehicule;
import com.logiflow.tms.fleet.domain.model.Vehicule;
import com.logiflow.tms.shared.domain.vo.Immatriculation;
import com.logiflow.tms.shared.domain.vo.Poids;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class VehiculeTest {

  private static final Immatriculation IMMAT = new Immatriculation("AB-123-CD");

  @Test
  void creerUnVehiculeEstDisponibleParDefaut() {
    Vehicule vehicule =
        Vehicule.creer(
            UUID.randomUUID(), IMMAT, TypeVehicule.PORTEUR, new Poids(19000), new Poids(9000));

    assertThat(vehicule.estDisponible()).isTrue();
    assertThat(vehicule.statut()).isEqualTo(StatutVehicule.DISPONIBLE);
    assertThat(vehicule.kilometrage()).isZero();
  }

  @Test
  void relerverUnKilometrageInferieurEchoue() {
    Vehicule vehicule =
        Vehicule.creer(
            UUID.randomUUID(), IMMAT, TypeVehicule.PORTEUR, new Poids(19000), new Poids(9000));
    vehicule.relever(1000, 50);

    assertThatThrownBy(() -> vehicule.relever(500, 60))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void changerStatutMetAJourLaDisponibilite() {
    Vehicule vehicule =
        Vehicule.creer(
            UUID.randomUUID(), IMMAT, TypeVehicule.TRACTEUR, new Poids(19000), new Poids(9000));

    vehicule.changerStatut(StatutVehicule.EN_MAINTENANCE);

    assertThat(vehicule.estDisponible()).isFalse();
  }

  @Test
  void sortirDuParcMetLeVehiculeHorsService() {
    Vehicule vehicule =
        Vehicule.creer(
            UUID.randomUUID(), IMMAT, TypeVehicule.TRACTEUR, new Poids(19000), new Poids(9000));

    vehicule.sortir(java.time.LocalDate.of(2026, 1, 1), "Réforme", 300000, 15000);

    assertThat(vehicule.statut()).isEqualTo(StatutVehicule.HORS_SERVICE);
    assertThat(vehicule.dateSortie()).isEqualTo(java.time.LocalDate.of(2026, 1, 1));
  }
}
