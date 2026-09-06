package com.logiflow.tms.dossier.infrastructure.web.dto;

import com.logiflow.tms.dossier.domain.model.DossierTransport;
import com.logiflow.tms.dossier.domain.model.TypeCarrosserieRequise;
import com.logiflow.tms.dossier.domain.model.TypeTransport;
import com.logiflow.tms.dossier.domain.vo.DocumentTransport;
import com.logiflow.tms.dossier.domain.vo.LigneMarchandise;
import com.logiflow.tms.dossier.domain.vo.Segment;
import java.util.List;
import java.util.UUID;

public record DossierResponse(
    UUID id,
    String reference,
    UUID commandeId,
    String statut,
    TypeTransport typeTransport,
    boolean groupable,
    double poidsBrutKg,
    double volumeM3,
    int nbPalettes,
    String familleMarchandise,
    TypeCarrosserieRequise carrosserieRequise,
    Double temperatureRequise,
    boolean contientAdr,
    List<LigneMarchandise> lignesMarchandise,
    List<Segment> segments,
    List<DocumentTransport> documents) {

  public static DossierResponse depuis(DossierTransport dossier, boolean contientAdr) {
    return new DossierResponse(
        dossier.id(),
        dossier.reference().valeur(),
        dossier.commandeId(),
        dossier.statut().name(),
        dossier.typeTransport(),
        dossier.groupable(),
        dossier.poidsBrutKg(),
        dossier.volumeM3(),
        dossier.nbPalettes(),
        dossier.familleMarchandise(),
        dossier.carrosserieRequise(),
        dossier.temperatureRequise(),
        contientAdr,
        dossier.lignesMarchandise(),
        dossier.segments(),
        dossier.documents());
  }
}
