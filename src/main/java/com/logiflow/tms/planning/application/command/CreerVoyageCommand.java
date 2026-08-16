package com.logiflow.tms.planning.application.command;

import com.logiflow.tms.planning.domain.model.Portee;
import com.logiflow.tms.planning.domain.model.TypeVoyage;
import com.logiflow.tms.planning.domain.vo.Affectation;
import com.logiflow.tms.planning.domain.vo.Trajet;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Commande applicative de création d'un voyage, ressources déjà choisies (affectation directe). */
public record CreerVoyageCommand(
    TypeVoyage typeVoyage,
    Portee portee,
    Instant departPrevu,
    Instant arriveePrevue,
    UUID vehiculeId,
    UUID remorqueId,
    List<UUID> dossierIds,
    Trajet trajet,
    List<Affectation> affectations) {}
