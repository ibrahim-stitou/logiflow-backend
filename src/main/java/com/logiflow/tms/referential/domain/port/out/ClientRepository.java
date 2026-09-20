package com.logiflow.tms.referential.domain.port.out;

import com.logiflow.tms.referential.domain.model.Client;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.Optional;
import java.util.UUID;

/** Port de sortie pour la persistance des clients, implémenté par l'adaptateur JPA. */
public interface ClientRepository {

  Client sauvegarder(Client client);

  Optional<Client> parId(UUID id);

  Optional<Client> parCode(String code);

  boolean existeParCode(String code);

  Page<Client> rechercher(String texteRecherche, PageRequest pageRequest);
}
