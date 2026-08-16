package com.logiflow.tms.iam.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.logiflow.tms.iam.domain.model.RoleUtilisateur;
import com.logiflow.tms.iam.domain.model.Utilisateur;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UtilisateurTest {

  @Test
  void creerUnUtilisateurEstActifParDefaut() {
    Utilisateur utilisateur =
        Utilisateur.creer(
            UUID.randomUUID(),
            " Jean.Dupont ",
            "jean.dupont@logiflow.tms",
            Set.of(RoleUtilisateur.EXPLOITANT));

    assertThat(utilisateur.login()).isEqualTo("jean.dupont");
    assertThat(utilisateur.estActif()).isTrue();
    assertThat(utilisateur.aLeRole(RoleUtilisateur.EXPLOITANT)).isTrue();
    assertThat(utilisateur.aLeRole(RoleUtilisateur.ADMINISTRATEUR)).isFalse();
  }

  @Test
  void creerUnUtilisateurAvecUnEmailInvalideEchoue() {
    assertThatThrownBy(
            () ->
                Utilisateur.creer(
                    UUID.randomUUID(),
                    "jean.dupont",
                    "pas-un-email",
                    Set.of(RoleUtilisateur.EXPLOITANT)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void creerUnUtilisateurSansRoleEchoue() {
    assertThatThrownBy(
            () ->
                Utilisateur.creer(UUID.randomUUID(), "jean.dupont", "jean@logiflow.tms", Set.of()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void desactiverPuisReactiverChangeLEtatDeLUtilisateur() {
    Utilisateur utilisateur =
        Utilisateur.creer(
            UUID.randomUUID(),
            "jean.dupont",
            "jean@logiflow.tms",
            Set.of(RoleUtilisateur.EXPLOITANT));

    utilisateur.desactiver();
    assertThat(utilisateur.estActif()).isFalse();

    utilisateur.activer();
    assertThat(utilisateur.estActif()).isTrue();
  }
}
