package com.logiflow.tms.order.infrastructure.web;

import com.logiflow.tms.order.application.CommandeService;
import com.logiflow.tms.order.application.command.CreerCommandeCommand;
import com.logiflow.tms.order.infrastructure.web.dto.CommandeRequest;
import com.logiflow.tms.order.infrastructure.web.dto.CommandeResponse;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.infrastructure.web.PageResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** API REST du sous-domaine Commande. */
@RestController
@RequiredArgsConstructor
public class CommandeController {

  private final CommandeService commandeService;

  @PostMapping("/api/v1/commandes")
  public ResponseEntity<CommandeResponse> creer(@Valid @RequestBody CommandeRequest request) {
    UUID id =
        commandeService.creerCommande(
            new CreerCommandeCommand(
                request.clientId(), request.dateSouhaitee(), request.prixNegocie(), request.lignes()));
    CommandeResponse reponse = CommandeResponse.depuis(commandeService.consulterCommande(id));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    return ResponseEntity.created(location).body(reponse);
  }

  @GetMapping("/api/v1/commandes/{id}")
  public CommandeResponse consulter(@PathVariable UUID id) {
    return CommandeResponse.depuis(commandeService.consulterCommande(id));
  }

  @GetMapping("/api/v1/commandes")
  public PageResponse<CommandeResponse> lister(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    var resultats = commandeService.listerCommandes(new PageRequest(page, size));
    return PageResponse.of(resultats, CommandeResponse::depuis);
  }

  @PutMapping("/api/v1/commandes/{id}/confirmer")
  public CommandeResponse confirmer(@PathVariable UUID id) {
    commandeService.confirmerCommande(id);
    return CommandeResponse.depuis(commandeService.consulterCommande(id));
  }

  @PutMapping("/api/v1/commandes/{id}/annuler")
  public CommandeResponse annuler(@PathVariable UUID id) {
    commandeService.annulerCommande(id);
    return CommandeResponse.depuis(commandeService.consulterCommande(id));
  }
}
