package com.logiflow.tms.maintenance.infrastructure.web.dto;

import com.logiflow.tms.maintenance.domain.model.TypeIntervention;
import com.logiflow.tms.shared.domain.vo.Money;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrdreTravailRequest(
    @NotNull UUID vehiculeId,
    @NotNull TypeIntervention type,
    @NotNull LocalDateTime datePlanifiee,
    @NotNull Money coutEstime) {}
