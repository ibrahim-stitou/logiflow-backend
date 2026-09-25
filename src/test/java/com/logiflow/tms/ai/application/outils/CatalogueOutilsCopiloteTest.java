package com.logiflow.tms.ai.application.outils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.logiflow.tms.shared.domain.exception.NotFoundException;
import com.logiflow.tms.shared.domain.exception.ValidationException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class CatalogueOutilsCopiloteTest {

  private record OutilFactice(String nom, Set<String> rolesAutorises) implements OutilCopilote {

    @Override
    public String libelle() {
      return nom;
    }

    @Override
    public String description() {
      return nom;
    }

    @Override
    public Map<String, Object> parametres() {
      return SchemaOutil.objet(SchemaOutil.limite());
    }

    @Override
    public ResultatOutil executer(ArgumentsOutil arguments) {
      return new ResultatOutil(List.of(Map.of("limite", arguments.limite())), 1, List.of());
    }
  }

  private final CatalogueOutilsCopilote catalogue =
      new CatalogueOutilsCopilote(
          List.of(
              new OutilFactice("rechercher_dossiers", RolesCopilote.EXPLOITATION_ET_COMMERCIAL),
              new OutilFactice("lister_vehicules", RolesCopilote.EXPLOITATION_ET_ATELIER),
              new OutilFactice("rechercher_voyages", RolesCopilote.EXPLOITATION)));

  @Test
  void leCatalogueEstFiltreParRole() {
    assertThat(catalogue.disponibles(Set.of("ATELIER")))
        .extracting(OutilCopilote::nom)
        .containsExactly("lister_vehicules");
    assertThat(catalogue.disponibles(Set.of("COMMERCIAL")))
        .extracting(OutilCopilote::nom)
        .containsExactly("rechercher_dossiers");
    assertThat(catalogue.disponibles(Set.of("EXPLOITANT"))).hasSize(3);
    assertThat(catalogue.disponibles(Set.of("CHAUFFEUR"))).isEmpty();
  }

  @Test
  void executerUnOutilHorsDroitsEstRefuse() {
    assertThatThrownBy(() -> catalogue.executer("rechercher_voyages", Map.of(), Set.of("ATELIER")))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void executerUnOutilInconnuLeveNotFound() {
    assertThatThrownBy(() -> catalogue.executer("supprimer_tout", Map.of(), Set.of("EXPLOITANT")))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void executerTransmetLesArguments() {
    var resultat = catalogue.executer("lister_vehicules", Map.of("limite", "3"), Set.of("ATELIER"));
    assertThat(resultat.resultats().getFirst()).containsEntry("limite", 3);
  }

  @Test
  void argumentsToleresSurLaFormeMaisStrictsSurLeFond() {
    Map<String, Object> valeurs = new HashMap<>();
    valeurs.put("statut", "en transit");
    valeurs.put("vide", "  ");
    valeurs.put("nul", null);
    valeurs.put("limite", 500);
    valeurs.put("debut", "2026-09-01");
    var arguments = new ArgumentsOutil(valeurs);

    assertThat(arguments.enumere("statut", List.of("CREE", "EN_TRANSIT"))).isEqualTo("EN_TRANSIT");
    assertThat(arguments.texte("vide")).isNull();
    assertThat(arguments.texte("nul")).isNull();
    assertThat(arguments.limite()).isEqualTo(ArgumentsOutil.LIMITE_MAX);
    assertThat(arguments.date("debut")).isEqualTo(LocalDate.of(2026, 9, 1));
    assertThatThrownBy(() -> arguments.enumere("statut", List.of("CREE")))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("valeurs possibles : CREE");
    assertThatThrownBy(() -> new ArgumentsOutil(Map.of("debut", "01/09/2026")).date("debut"))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("AAAA-MM-JJ");
  }
}
