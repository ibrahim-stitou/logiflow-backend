package com.logiflow.tms.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.library.GeneralCodingRules;
import jakarta.persistence.Entity;
import java.util.Date;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/** Règles de code transverses, complémentaires aux règles d'architecture hexagonale. */
class CodingRulesTest {

  private static JavaClasses classes;

  @BeforeAll
  static void importerClasses() {
    classes =
        new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.logiflow.tms");
  }

  @Test
  void pasDInjectionParChamp() {
    noFields()
        .should()
        .beAnnotatedWith(Autowired.class)
        .because("l'injection de dépendances se fait exclusivement par constructeur")
        .check(classes);
  }

  @Test
  void pasDAccesAuxFluxStandard() {
    GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS.check(classes);
  }

  @Test
  void pasDeJavaUtilDate() {
    noClasses()
        .should()
        .dependOnClassesThat()
        .belongToAnyOf(Date.class)
        .because("java.time.* doit être utilisé à la place de java.util.Date")
        .check(classes);
  }

  @Test
  void lesControleursSontAnnotesEtSuffixesController() {
    classes()
        .that()
        .areAnnotatedWith(RestController.class)
        .should()
        .haveSimpleNameEndingWith("Controller")
        .check(classes);
  }

  @Test
  void lesEntitesJpaResidentDansInfrastructurePersistenceEntity() {
    classes()
        .that()
        .areAnnotatedWith(Entity.class)
        .should()
        .resideInAPackage("..infrastructure.persistence.entity..")
        .check(classes);
  }
}
