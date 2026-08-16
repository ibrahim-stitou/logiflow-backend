package com.logiflow.tms.dossier.application.command;

import com.logiflow.tms.dossier.domain.model.TypeCarrosserieRequise;
import com.logiflow.tms.dossier.domain.model.TypeTransport;
import com.logiflow.tms.dossier.domain.vo.DocumentTransport;
import com.logiflow.tms.dossier.domain.vo.LigneMarchandise;
import com.logiflow.tms.dossier.domain.vo.Segment;
import java.util.List;
import java.util.UUID;

/** Commande applicative de création d'un dossier de transport. */
public record CreerDossierCommand(
    UUID commandeId,
    TypeTransport typeTransport,
    boolean groupable,
    int nbPalettes,
    String familleMarchandise,
    TypeCarrosserieRequise carrosserieRequise,
    Double temperatureRequise,
    List<LigneMarchandise> lignesMarchandise,
    List<Segment> segments,
    List<DocumentTransport> documents) {}
