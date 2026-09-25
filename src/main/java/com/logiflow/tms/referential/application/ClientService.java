package com.logiflow.tms.referential.application;

import com.logiflow.tms.referential.api.ClientApi;
import com.logiflow.tms.referential.api.dto.ClientSummary;
import com.logiflow.tms.referential.domain.model.Client;
import com.logiflow.tms.referential.domain.port.out.ClientRepository;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.ConflictException;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cas d'utilisation applicatifs du sous-domaine Client. */
@Service
@RequiredArgsConstructor
public class ClientService implements ClientApi {

  private final ClientRepository clientRepository;

  @Transactional
  public UUID creerClient(String code, String raisonSociale) {
    if (clientRepository.existeParCode(code)) {
      throw new ConflictException("Un client avec le code '%s' existe déjà".formatted(code));
    }
    Client client = Client.creer(UUID.randomUUID(), code, raisonSociale);
    return clientRepository.sauvegarder(client).id();
  }

  @Transactional
  public void renommerClient(UUID id, String raisonSociale) {
    Client client = trouverOuEchouer(id);
    client.renommer(raisonSociale);
    clientRepository.sauvegarder(client);
  }

  @Transactional
  public void desactiverClient(UUID id) {
    Client client = trouverOuEchouer(id);
    client.desactiver();
    clientRepository.sauvegarder(client);
  }

  @Transactional(readOnly = true)
  public Client consulterClient(UUID id) {
    return trouverOuEchouer(id);
  }

  @Transactional(readOnly = true)
  public Page<Client> listerClients(String texteRecherche, PageRequest pageRequest) {
    return clientRepository.rechercher(texteRecherche, pageRequest);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ClientSummary> consulter(UUID clientId) {
    return clientRepository.parId(clientId).map(this::versResume);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ClientSummary> rechercher(String texte, PageRequest pageRequest) {
    return clientRepository.rechercher(texte, pageRequest).map(this::versResume);
  }

  private ClientSummary versResume(Client client) {
    return new ClientSummary(client.id(), client.code(), client.raisonSociale(), client.estActif());
  }

  @Override
  @Transactional(readOnly = true)
  public boolean estActif(UUID clientId) {
    return clientRepository.parId(clientId).map(Client::estActif).orElse(false);
  }

  private Client trouverOuEchouer(UUID id) {
    return clientRepository
        .parId(id)
        .orElseThrow(() -> new NotFoundException("Aucun client trouvé pour l'identifiant " + id));
  }
}
