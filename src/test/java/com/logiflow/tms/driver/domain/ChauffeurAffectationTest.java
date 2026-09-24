package com.logiflow.tms.driver.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.logiflow.tms.driver.domain.model.CategoriePermis;
import com.logiflow.tms.driver.domain.model.Chauffeur;
import com.logiflow.tms.driver.domain.model.DisponibiliteChauffeur;
import com.logiflow.tms.driver.domain.model.ExigencesAffectation;
import com.logiflow.tms.driver.domain.model.ProfilChauffeur;
import com.logiflow.tms.driver.domain.model.StatutChauffeur;
import com.logiflow.tms.driver.domain.vo.Habilitation;
import com.logiflow.tms.driver.domain.vo.Habilitation.TypeHabilitation;
import java.time.Duration;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChauffeurAffectationTest {

  private static final LocalDate DEPART = LocalDate.of(2026, 10, 5);

  private static ProfilChauffeur profil(
      Set<CategoriePermis> categories, String passeport, LocalDate expirationPasseport) {
    return new ProfilChauffeur(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        "P-1",
        categories,
        null,
        LocalDate.of(2030, 1, 1),
        passeport,
        null,
        expirationPasseport,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null);
  }

  private static Chauffeur chauffeur(ProfilChauffeur profil, List<Habilitation> habilitations) {
    return Chauffeur.creer(
        UUID.randomUUID(), "drv-1", "Martin", "Paul", profil, habilitations, Duration.ofHours(40));
  }

  private static Habilitation habilitation(TypeHabilitation type, LocalDate expiration) {
    return new Habilitation(type, "REF-" + type, LocalDate.of(2020, 1, 1), expiration);
  }

  @Test
  void unChauffeurActifDisponibleSansExigenceEstAffectable() {
    Chauffeur chauffeur = chauffeur(ProfilChauffeur.vide(), List.of());

    assertThat(chauffeur.motifsNonAffectation(new ExigencesAffectation(DEPART, false, false, null)))
        .isEmpty();
    assertThat(chauffeur.matricule()).isEqualTo("DRV-1");
  }

  @Test
  void statutSuspenduEtIndisponibiliteBloquent() {
    Chauffeur chauffeur = chauffeur(ProfilChauffeur.vide(), List.of());
    chauffeur.changerStatut(StatutChauffeur.SUSPENDU);
    chauffeur.changerDisponibilite(DisponibiliteChauffeur.EN_CONGE);

    assertThat(chauffeur.motifsNonAffectation(new ExigencesAffectation(DEPART, false, false, null)))
        .containsExactly(
            "Chauffeur DRV-1 : statut SUSPENDU", "Chauffeur DRV-1 : non disponible (EN_CONGE)");
  }

  @Test
  void enVoyageNeBloquePasCarLeChevauchementEstControleParPlanning() {
    Chauffeur chauffeur = chauffeur(ProfilChauffeur.vide(), List.of());
    chauffeur.changerDisponibilite(DisponibiliteChauffeur.EN_VOYAGE);

    assertThat(chauffeur.motifsNonAffectation(new ExigencesAffectation(DEPART, false, false, null)))
        .isEmpty();
  }

  @Test
  void adrExigeUneHabilitationAdrValideALaDateDuDepart() {
    Chauffeur sansAdr = chauffeur(ProfilChauffeur.vide(), List.of());
    Chauffeur adrExpiree =
        chauffeur(
            ProfilChauffeur.vide(),
            List.of(habilitation(TypeHabilitation.ADR_BASE, DEPART.minusDays(1))));
    Chauffeur adrValide =
        chauffeur(
            ProfilChauffeur.vide(),
            List.of(habilitation(TypeHabilitation.ADR_BASE, DEPART.plusYears(1))));
    var exigences = new ExigencesAffectation(DEPART, true, false, null);

    assertThat(sansAdr.motifsNonAffectation(exigences)).singleElement().asString().contains("ADR");
    assertThat(adrExpiree.motifsNonAffectation(exigences)).hasSize(1);
    assertThat(adrValide.motifsNonAffectation(exigences)).isEmpty();
  }

  @Test
  void uneHabilitationDeclareeMaisExpireeBloqueUneAbsenteNon() {
    Chauffeur visiteExpiree =
        chauffeur(
            ProfilChauffeur.vide(),
            List.of(habilitation(TypeHabilitation.VISITE_MEDICALE, DEPART.minusDays(3))));

    assertThat(
            visiteExpiree.motifsNonAffectation(
                new ExigencesAffectation(DEPART, false, false, null)))
        .singleElement()
        .asString()
        .contains("VISITE_MEDICALE");
  }

  @Test
  void permisRequisVerifieQuandLesCategoriesSontConnues() {
    var exigences = new ExigencesAffectation(DEPART, false, false, CategoriePermis.CE);

    assertThat(
            chauffeur(profil(EnumSet.of(CategoriePermis.C), null, null), List.of())
                .motifsNonAffectation(exigences))
        .containsExactly("Chauffeur DRV-1 : permis CE requis");
    assertThat(
            chauffeur(profil(EnumSet.of(CategoriePermis.CE), null, null), List.of())
                .motifsNonAffectation(exigences))
        .isEmpty();
    // Catégories non renseignées (données historiques) : non bloquant.
    assertThat(chauffeur(ProfilChauffeur.vide(), List.of()).motifsNonAffectation(exigences))
        .isEmpty();
  }

  @Test
  void internationalExigeUnPasseportValide() {
    var exigences = new ExigencesAffectation(DEPART, false, true, null);

    assertThat(chauffeur(ProfilChauffeur.vide(), List.of()).motifsNonAffectation(exigences))
        .singleElement()
        .asString()
        .contains("passeport");
    assertThat(
            chauffeur(profil(Set.of(), "PA123", DEPART.plusYears(2)), List.of())
                .motifsNonAffectation(exigences))
        .isEmpty();
  }

  @Test
  void categoriesDePermisLuesDeFaconTolerante() {
    assertThat(CategoriePermis.depuisTexte(" ce ; C, inconnu"))
        .containsExactlyInAnyOrder(CategoriePermis.C, CategoriePermis.CE);
    assertThat(CategoriePermis.versTexte(EnumSet.of(CategoriePermis.CE, CategoriePermis.C)))
        .isEqualTo("C,CE");
    assertThat(CategoriePermis.C.estCouvertePar(EnumSet.of(CategoriePermis.CE))).isTrue();
    assertThat(CategoriePermis.CE.estCouvertePar(EnumSet.of(CategoriePermis.C))).isFalse();
  }

  @Test
  void leProfilRefuseDesDatesIncoherentes() {
    assertThatCode(() -> profil(Set.of(), "PA1", LocalDate.of(2000, 1, 1)))
        .doesNotThrowAnyException();
    assertThatThrownBy(
            () ->
                new ProfilChauffeur(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    LocalDate.of(2030, 1, 1),
                    LocalDate.of(2020, 1, 1),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("permis");
  }
}
