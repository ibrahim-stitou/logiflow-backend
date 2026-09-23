package com.logiflow.tms.ai.application.outils;

import com.logiflow.tms.shared.domain.exception.NotFoundException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registre des outils du copilote, filtré par les rôles de l'utilisateur. */
@Service
public class CatalogueOutilsCopilote {

  private final Map<String, OutilCopilote> outils;

  public CatalogueOutilsCopilote(List<OutilCopilote> outils) {
    this.outils =
        outils.stream()
            .collect(Collectors.toUnmodifiableMap(OutilCopilote::nom, Function.identity()));
  }

  /** Outils accessibles avec au moins un des rôles donnés (sans préfixe ROLE_). */
  public List<OutilCopilote> disponibles(Set<String> roles) {
    return outils.values().stream()
        .filter(outil -> autorise(outil, roles))
        .sorted(Comparator.comparing(OutilCopilote::nom))
        .toList();
  }

  /**
   * @throws NotFoundException outil inconnu
   * @throws AccessDeniedException outil hors des droits de l'utilisateur
   */
  @Transactional(readOnly = true)
  public ResultatOutil executer(String nom, Map<String, Object> arguments, Set<String> roles) {
    OutilCopilote outil = outils.get(nom);
    if (outil == null) {
      throw new NotFoundException("Outil inconnu : " + nom);
    }
    if (!autorise(outil, roles)) {
      throw new AccessDeniedException("Outil non autorisé pour cet utilisateur : " + nom);
    }
    return outil.executer(new ArgumentsOutil(arguments));
  }

  private static boolean autorise(OutilCopilote outil, Collection<String> roles) {
    return roles.stream().anyMatch(outil.rolesAutorises()::contains);
  }
}
