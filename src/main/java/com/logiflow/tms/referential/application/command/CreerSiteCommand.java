package com.logiflow.tms.referential.application.command;

import com.logiflow.tms.referential.domain.model.Horaires;
import com.logiflow.tms.referential.domain.vo.ContraintesAcces;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.util.List;
import java.util.UUID;

/** Commande applicative de création d'un site. */
public record CreerSiteCommand(
    String code,
    String libelle,
    UUID clientId,
    GeoPoint localisation,
    String adresse,
    ContraintesAcces contraintesAcces,
    List<Horaires.CreneauHoraire> creneaux) {}
