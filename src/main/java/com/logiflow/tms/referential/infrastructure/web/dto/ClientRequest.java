package com.logiflow.tms.referential.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClientRequest(
    @NotBlank @Size(max = 50) String code, @NotBlank @Size(max = 255) String raisonSociale) {}
