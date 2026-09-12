package com.logiflow.tms.order.application;

import com.logiflow.tms.order.api.CommandeApi;
import com.logiflow.tms.order.api.dto.CommandeSummary;
import com.logiflow.tms.order.application.command.CreerCommandeCommand;
import com.logiflow.tms.order.domain.model.Commande;
import com.logiflow.tms.order.domain.port.out.CommandeRepository;
import com.logiflow.tms.order.domain.port.out.SequenceReferenceGenerator;
import com.logiflow.tms.order.domain.service.OrderDomainService;
import com.logiflow.tms.order.domain.vo.LigneCommande;
import com.logiflow.tms.referential.api.ClientApi;
import com.logiflow.tms.referential.api.MarchandiseApi;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import java.time.LocalDate;
import java.time.Year;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cas d'utilisation applicatifs du sous-domaine Commande. */
@Service
@RequiredArgsConstructor
public class CommandeService implements CommandeApi {

  private static final String PREFIXE_REFERENCE = "CMD";

  private final CommandeRepository commandeRepository;
  private final OrderDomainService orderDomainService;
  private final SequenceReferenceGenerator referenceGenerator;
  private final ClientApi clientApi;
  private final MarchandiseApi marchandiseApi;

  @Transactional
  public UUID creerCommande(CreerCommandeCommand command) {
    if (!clientApi.estActif(command.clientId())) {
      throw new NotFoundException(
          "Aucun client actif trouvé pour l'identifiant " + command.clientId());
    }
    orderDomainService.verifierDateSouhaitee(command.dateSouhaitee(), LocalDate.now());
    for (LigneCommande ligne : command.lignes()) {
      if (!marchandiseApi.estActif(ligne.marchandiseId())) {
        throw new NotFoundException(
            "Aucune marchandise active trouvée pour l'identifiant " + ligne.marchandiseId());
      }
    }

    var reference = referenceGenerator.generer(PREFIXE_REFERENCE, Year.now().getValue());
    Commande commande =
        Commande.creer(
            UUID.randomUUID(),
            reference,
            command.clientId(),
            command.dateSouhaitee(),
            command.prixNegocie(),
            command.lignes());
    return commandeRepository.sauvegarder(commande).id();
  }

  @Transactional
  public void confirmerCommande(UUID id) {
    Commande commande = trouverOuEchouer(id);
    commande.confirmer();
    commandeRepository.sauvegarder(commande);
  }

  @Transactional
  public void annulerCommande(UUID id) {
    Commande commande = trouverOuEchouer(id);
    commande.annuler();
    commandeRepository.sauvegarder(commande);
  }

  @Transactional(readOnly = true)
  public Commande consulterCommande(UUID id) {
    return trouverOuEchouer(id);
  }

  @Transactional(readOnly = true)
  public Page<Commande> listerCommandes(String texteRecherche, PageRequest pageRequest) {
    return commandeRepository.rechercher(texteRecherche, pageRequest);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<CommandeSummary> consulter(UUID commandeId) {
    return commandeRepository.parId(commandeId).map(this::versResume);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean estConfirmee(UUID commandeId) {
    return commandeRepository.parId(commandeId).map(Commande::estConfirmee).orElse(false);
  }

  private CommandeSummary versResume(Commande commande) {
    return new CommandeSummary(
        commande.id(),
        commande.reference().valeur(),
        commande.clientId(),
        commande.statut().name(),
        commande.dateSouhaitee());
  }

  private Commande trouverOuEchouer(UUID id) {
    return commandeRepository
        .parId(id)
        .orElseThrow(
            () -> new NotFoundException("Aucune commande trouvée pour l'identifiant " + id));
  }
}
