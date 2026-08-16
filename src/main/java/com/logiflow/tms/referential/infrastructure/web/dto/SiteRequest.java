package com.logiflow.tms.referential.infrastructure.web.dto;

import com.logiflow.tms.referential.domain.vo.ContraintesAcces;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/** Requête de création ou de mise à jour d'un site. Le code est ignoré lors d'une mise à jour. */
public record SiteRequest(
    @NotBlank @Size(max = 50) String code,
    @NotBlank @Size(max = 255) String libelle,
    UUID clientId,
    @NotNull @Valid GeoPoint localisation,
    @Size(max = 500) String adresse,
    @Valid ContraintesAcces contraintesAcces,
    List<@Valid CreneauRequest> horaires) {

  public record CreneauRequest(
      @NotNull DayOfWeek jour, @NotNull LocalTime debut, @NotNull LocalTime fin) {}
}
