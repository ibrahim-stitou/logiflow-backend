package com.logiflow.tms.fleet.infrastructure.web;

import com.logiflow.tms.fleet.application.RemorqueService;
import com.logiflow.tms.fleet.application.command.CreerRemorqueCommand;
import com.logiflow.tms.fleet.domain.model.TypeCarrosserie;
import com.logiflow.tms.fleet.domain.model.TypeRemorque;
import com.logiflow.tms.fleet.infrastructure.web.dto.RemorqueRequest;
import com.logiflow.tms.fleet.infrastructure.web.dto.RemorqueResponse;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.infrastructure.web.PageResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
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

/** API REST du sous-domaine Remorque. */
@RestController
@RequiredArgsConstructor
public class RemorqueController {

  private final RemorqueService remorqueService;

  @PostMapping("/api/v1/remorques")
  public ResponseEntity<RemorqueResponse> creer(@Valid @RequestBody RemorqueRequest request) {
    UUID id =
        remorqueService.creerRemorque(
            new CreerRemorqueCommand(
                request.immatriculation(),
                request.type() != null ? TypeRemorque.valueOf(request.type()) : null,
                TypeCarrosserie.valueOf(request.carrosserie()),
                request.numeroParc(),
                request.vin(),
                request.marque(),
                request.modele(),
                request.anneeFabrication(),
                request.poidsVideKg(),
                request.volumeUtileM3(),
                request.nbPositionsPalettes(),
                request.chargeUtileKg(),
                request.longueurM(),
                request.largeurM(),
                request.hauteurM(),
                request.groupeFroid(),
                request.temperatureMin(),
                request.temperatureMax(),
                request.datePremiereMiseCirculation(),
                request.dateAcquisition(),
                request.dateMiseEnService()));
    RemorqueResponse reponse = RemorqueResponse.depuis(remorqueService.consulterRemorque(id));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    return ResponseEntity.created(location).body(reponse);
  }

  @GetMapping("/api/v1/remorques/{id}")
  public RemorqueResponse consulter(@PathVariable UUID id) {
    return RemorqueResponse.depuis(remorqueService.consulterRemorque(id));
  }

  @GetMapping("/api/v1/remorques")
  public PageResponse<RemorqueResponse> lister(
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    var resultats = remorqueService.listerRemorques(q, new PageRequest(page, size));
    return PageResponse.of(resultats, RemorqueResponse::depuis);
  }

  @PutMapping("/api/v1/remorques/{id}/compteurs")
  public RemorqueResponse relever(
      @PathVariable UUID id, @RequestParam int kilometrage, @RequestParam int heuresGroupeFroid) {
    remorqueService.relever(id, kilometrage, heuresGroupeFroid);
    return RemorqueResponse.depuis(remorqueService.consulterRemorque(id));
  }

  @PutMapping("/api/v1/remorques/{id}/sortie")
  public RemorqueResponse sortir(
      @PathVariable UUID id,
      @RequestParam LocalDate dateSortie,
      @RequestParam(required = false) String motifSortie,
      @RequestParam(required = false) Integer kilometrageSortie,
      @RequestParam(required = false) Integer heuresGroupeFroidSortie) {
    remorqueService.sortir(id, dateSortie, motifSortie, kilometrageSortie, heuresGroupeFroidSortie);
    return RemorqueResponse.depuis(remorqueService.consulterRemorque(id));
  }
}
