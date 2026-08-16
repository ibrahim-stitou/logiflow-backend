package com.logiflow.tms.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Fait respecter les 7 règles d'architecture hexagonale + modulaire décrites dans
 * docs/architecture.md. Chaque méthode vérifie une règle indépendante.
 */
class HexagonalArchitectureTest {

  private static JavaClasses classes;

  @BeforeAll
  static void importerClasses() {
    classes =
        new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.logiflow.tms");
  }

  @Test
  void regle1_domaineSansDependanceFramework() {
    noClasses()
        .that()
        .resideInAPackage("..domain..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "org.springframework..",
            "jakarta.persistence..",
            "com.fasterxml.jackson..",
            "tools.jackson..",
            "lombok..")
        .because("le modèle de domaine doit rester indépendant de tout framework technique")
        .check(classes);
  }

  @Test
  void regle2_applicationNeDependQueDuDomaine() {
    noClasses()
        .that()
        .resideInAPackage("..application..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..infrastructure..")
        .because(
            "la couche application orchestre le domaine via des ports, jamais l'infrastructure directement")
        .check(classes);
  }

  @Test
  void regle3_infrastructureNeRemonteJamaisVersLApplicationOuLeDomaine() {
    noClasses()
        .that()
        .resideInAPackage("..domain..")
        .or()
        .resideInAPackage("..application..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..infrastructure..")
        .because("domain et application ne dépendent jamais de infrastructure")
        .check(classes);
  }

  @Test
  void regle4_pasDeCycleEntreModules() {
    SlicesRuleDefinition.slices()
        .matching("com.logiflow.tms.(*)..")
        .should()
        .beFreeOfCycles()
        .check(classes);
  }

  private static final List<String> MODULES_METIER =
      List.of(
          "iam",
          "referential",
          "fleet",
          "driver",
          "order",
          "dossier",
          "planning",
          "maintenance",
          "tracking");

  @Test
  void regle5_unModuleNAccedeQuALApiOuAuxEvenementsDesAutresModules() {
    for (String module : MODULES_METIER) {
      DescribedPredicate<JavaClass> autresModulesHorsApi =
          MODULES_METIER.stream()
              .filter(autre -> !autre.equals(module))
              .map(
                  autre ->
                      JavaClass.Predicates.resideInAPackage("com.logiflow.tms." + autre + "..")
                          .and(
                              DescribedPredicate.not(
                                  JavaClass.Predicates.resideInAPackage(
                                      "com.logiflow.tms." + autre + ".api.."))))
              .reduce(DescribedPredicate.alwaysFalse(), (a, b) -> a.or(b));

      noClasses()
          .that()
          .resideInAPackage("com.logiflow.tms." + module + "..")
          .should()
          .dependOnClassesThat(autresModulesHorsApi)
          .because(
              "un module ne doit accéder aux autres modules que via leur package api (ou un événement)")
          .check(classes);
    }
  }

  @Test
  void regle6_lesEntitesJpaRestentDansLInfrastructure() {
    noClasses()
        .that()
        .resideOutsideOfPackage("..infrastructure.persistence..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..infrastructure.persistence.entity..")
        .because("les entités JPA ne sortent jamais de la couche infrastructure.persistence")
        .check(classes);
  }

  @Test
  void regle7_pasDInjectionParChamp() {
    noFields()
        .should()
        .beAnnotatedWith(Autowired.class)
        .because("l'injection de dépendances se fait exclusivement par constructeur")
        .check(classes);
  }
}
