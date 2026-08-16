package com.logiflow.tms.iam.infrastructure.persistence.mapper;

import com.logiflow.tms.iam.domain.model.RoleUtilisateur;
import com.logiflow.tms.iam.domain.model.Utilisateur;
import com.logiflow.tms.iam.infrastructure.persistence.entity.UtilisateurEntity;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Traduit entre le modèle de domaine {@link Utilisateur} et l'entité JPA {@link UtilisateurEntity}.
 * Écrit à la main pour les mêmes raisons que les autres mappers du projet.
 */
@Component
@RequiredArgsConstructor
public class UtilisateurMapper {

  // TODO vérifier que le tenant courant sera lu depuis le contexte de sécurité une fois le
  // multi-tenant implémenté ; en attendant, un tenant par défaut est utilisé.
  private static final UUID TENANT_PAR_DEFAUT =
      UUID.fromString("00000000-0000-0000-0000-000000000000");

  private final ObjectMapper objectMapper;

  public Utilisateur versDomaine(UtilisateurEntity entity) {
    if (entity == null) {
      return null;
    }
    return Utilisateur.reconstituer(
        entity.getId(),
        entity.getLogin(),
        entity.getEmail(),
        versRoles(entity.getRolesJson()),
        entity.isActif());
  }

  public UtilisateurEntity versEntite(Utilisateur utilisateur) {
    if (utilisateur == null) {
      return null;
    }
    return UtilisateurEntity.builder()
        .id(utilisateur.id())
        .tenantId(TENANT_PAR_DEFAUT)
        .login(utilisateur.login())
        .email(utilisateur.email())
        .rolesJson(versJson(utilisateur.roles()))
        .actif(utilisateur.estActif())
        .build();
  }

  private String versJson(Object valeur) {
    try {
      return objectMapper.writeValueAsString(valeur);
    } catch (JacksonException e) {
      throw new IllegalStateException("Échec de sérialisation JSON en persistance", e);
    }
  }

  private Set<RoleUtilisateur> versRoles(String json) {
    if (json == null || json.isBlank()) {
      return Set.of();
    }
    try {
      return objectMapper.readValue(json, new TypeReference<Set<RoleUtilisateur>>() {});
    } catch (JacksonException e) {
      throw new IllegalStateException("Échec de désérialisation des rôles", e);
    }
  }
}
