package com.logiflow.tms.referential.infrastructure.web;

import com.logiflow.tms.referential.application.MarchandiseService;
import com.logiflow.tms.referential.infrastructure.web.dto.MarchandiseRequest;
import com.logiflow.tms.referential.infrastructure.web.dto.MarchandiseResponse;
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

/** API REST du sous-domaine Marchandise (catalogue référentiel). */
@RestController
@RequiredArgsConstructor
public class MarchandiseController {

  private final MarchandiseService marchandiseService;

  @PostMapping("/api/v1/marchandises")
  public ResponseEntity<MarchandiseResponse> creer(@Valid @RequestBody MarchandiseRequest request) {
    UUID id =
        marchandiseService.creerMarchandise(
            request.code(),
            request.libelle(),
            request.famille(),
            request.classeAdr(),
            request.numeroOnu(),
            request.gerbable());
    MarchandiseResponse reponse =
        MarchandiseResponse.depuis(marchandiseService.consulterMarchandise(id));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    return ResponseEntity.created(location).body(reponse);
  }

  @GetMapping("/api/v1/marchandises/{id}")
  public MarchandiseResponse consulter(@PathVariable UUID id) {
    return MarchandiseResponse.depuis(marchandiseService.consulterMarchandise(id));
  }

  @GetMapping("/api/v1/marchandises")
  public PageResponse<MarchandiseResponse> lister(
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    var resultats = marchandiseService.listerMarchandises(q, new PageRequest(page, size));
    return PageResponse.of(resultats, MarchandiseResponse::depuis);
  }

  @DeleteMapping("/api/v1/marchandises/{id}")
  public ResponseEntity<Void> desactiver(@PathVariable UUID id) {
    marchandiseService.desactiverMarchandise(id);
    return ResponseEntity.noContent().build();
  }
}
