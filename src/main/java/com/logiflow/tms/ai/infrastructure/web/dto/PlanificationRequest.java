package com.logiflow.tms.ai.infrastructure.web.dto;

import com.logiflow.tms.ai.application.command.ProposerVoyagesCommand;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;

/** Demande de propositions de voyages sur une période. */
public record PlanificationRequest(
    @NotNull Instant debut,
    @NotNull Instant fin,
    @Pattern(regexp = "SIMPLE|GROUPAGE|RAMASSE|DISTRIBUTION|NAVETTE") String typeVoyage,
    @Pattern(regexp = "NATIONAL|INTERNATIONAL") String portee,
    @Min(1) @Max(5) Integer nbOptions) {

  public ProposerVoyagesCommand versCommande() {
    return new ProposerVoyagesCommand(
        debut,
        fin,
        typeVoyage == null ? "GROUPAGE" : typeVoyage,
        portee == null ? "NATIONAL" : portee,
        nbOptions == null ? 3 : nbOptions);
  }
}
