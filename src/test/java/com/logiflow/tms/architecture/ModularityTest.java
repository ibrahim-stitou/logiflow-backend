package com.logiflow.tms.architecture;

import com.logiflow.tms.LogiflowApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Vérifie l'intégrité de la structure modulaire (encapsulation, absence de cycles) et génère la
 * documentation des modules dans {@code target/spring-modulith-docs}.
 */
class ModularityTest {

  private static final ApplicationModules MODULES =
      ApplicationModules.of(LogiflowApplication.class);

  @Test
  void laStructureModulaireEstValide() {
    MODULES.verify();
  }

  @Test
  void genererLaDocumentationDesModules() {
    new Documenter(MODULES).writeDocumentation();
  }
}
