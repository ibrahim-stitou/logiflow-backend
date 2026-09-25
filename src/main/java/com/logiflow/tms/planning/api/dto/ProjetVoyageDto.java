package com.logiflow.tms.planning.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Projet de voyage soumis au contrôle de conformité par un autre module (agent de planification).
 * {@code chauffeurIds} : le premier est titulaire, les suivants en renfort ; {@code ordreSites}
 * impose l'ordre des arrêts (null = déduit des fenêtres des dossiers).
 */
public record ProjetVoyageDto(
    String typeVoyage,
    String portee,
    Instant departPrevu,
    Instant arriveePrevue,
    UUID vehiculeId,
    UUID remorqueId,
    List<UUID> dossierIds,
    List<UUID> chauffeurIds,
    int dureeConduiteMin,
    List<UUID> ordreSites) {}
