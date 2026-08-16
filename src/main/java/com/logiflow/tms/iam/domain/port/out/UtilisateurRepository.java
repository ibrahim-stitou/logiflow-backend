package com.logiflow.tms.iam.domain.port.out;

import com.logiflow.tms.iam.domain.model.Utilisateur;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.Optional;
import java.util.UUID;

/** Port de sortie pour la persistance des utilisateurs. */
public interface UtilisateurRepository {

  Utilisateur sauvegarder(Utilisateur utilisateur);

  Optional<Utilisateur> parId(UUID id);

  Optional<Utilisateur> parLogin(String login);

  boolean existeParLogin(String login);

  Page<Utilisateur> rechercher(String texteRecherche, PageRequest pageRequest);
}
