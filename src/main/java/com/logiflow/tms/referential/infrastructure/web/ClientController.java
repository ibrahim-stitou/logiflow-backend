package com.logiflow.tms.referential.infrastructure.web;

import com.logiflow.tms.referential.application.ClientService;
import com.logiflow.tms.referential.infrastructure.web.dto.ClientRequest;
import com.logiflow.tms.referential.infrastructure.web.dto.ClientResponse;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.infrastructure.web.PageResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** API REST du sous-domaine Client. */
@RestController
@RequiredArgsConstructor
public class ClientController {

  private final ClientService clientService;

  @PostMapping("/api/v1/clients")
  public ResponseEntity<ClientResponse> creer(@Valid @RequestBody ClientRequest request) {
    UUID id = clientService.creerClient(request.code(), request.raisonSociale());
    ClientResponse reponse = ClientResponse.depuis(clientService.consulterClient(id));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    return ResponseEntity.created(location).body(reponse);
  }

  @GetMapping("/api/v1/clients")
  public PageResponse<ClientResponse> lister(
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    var resultats = clientService.listerClients(q, new PageRequest(page, size));
    return PageResponse.of(resultats, ClientResponse::depuis);
  }

  @GetMapping("/api/v1/clients/{id}")
  public ClientResponse consulter(@PathVariable UUID id) {
    return ClientResponse.depuis(clientService.consulterClient(id));
  }

  @DeleteMapping("/api/v1/clients/{id}")
  public ResponseEntity<Void> desactiver(@PathVariable UUID id) {
    clientService.desactiverClient(id);
    return ResponseEntity.noContent().build();
  }
}
