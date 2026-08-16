/**
 * Noyau technique partagé : value objects communs, exceptions, audit, gestion d'erreurs.
 *
 * <p>Module Spring Modulith de type {@code OPEN} : contrairement aux autres modules métier, tous
 * ses packages (et pas seulement {@code api}) sont visibles depuis n'importe quel autre module,
 * puisqu'il constitue le socle technique commun à l'application.
 */
@org.springframework.modulith.ApplicationModule(
    type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package com.logiflow.tms.shared;
