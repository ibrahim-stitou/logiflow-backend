package com.logiflow.tms.tracking.infrastructure.web.dto;

import com.logiflow.tms.shared.domain.vo.GeoPoint;
import com.logiflow.tms.tracking.domain.model.TypeEvenement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record EvenementVoyageRequest(
    @NotNull UUID voyageId,
    @NotNull TypeEvenement type,
    @NotNull Instant horodatage,
    @Valid GeoPoint position,
    String commentaire) {}
