package com.logiflow.tms.dossier.infrastructure.web.dto;

import com.logiflow.tms.dossier.domain.model.TypeCarrosserieRequise;
import com.logiflow.tms.dossier.domain.model.TypeTransport;
import com.logiflow.tms.dossier.domain.vo.DocumentTransport;
import com.logiflow.tms.dossier.domain.vo.LigneMarchandise;
import com.logiflow.tms.dossier.domain.vo.Segment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.UUID;

/** Requête de création d'un dossier de transport. */
public record DossierRequest(
    @NotNull UUID commandeId,
    @NotNull TypeTransport typeTransport,
    boolean groupable,
    @PositiveOrZero int nbPalettes,
    @NotBlank String familleMarchandise,
    TypeCarrosserieRequise carrosserieRequise,
    Double temperatureRequise,
    @NotEmpty List<@Valid LigneMarchandise> lignesMarchandise,
    @NotEmpty List<@Valid Segment> segments,
    List<@Valid DocumentTransport> documents) {}
