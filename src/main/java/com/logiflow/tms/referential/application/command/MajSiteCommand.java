package com.logiflow.tms.referential.application.command;

import com.logiflow.tms.referential.domain.model.Horaires;
import com.logiflow.tms.referential.domain.vo.ContraintesAcces;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.util.List;
import java.util.UUID;

/** Commande applicative de mise à jour d'un site existant (le code n'est pas modifiable). */
public record MajSiteCommand(
    String libelle,
    UUID clientId,
    GeoPoint localisation,
    String adresse,
    ContraintesAcces contraintesAcces,
    List<Horaires.CreneauHoraire> creneaux) {}
