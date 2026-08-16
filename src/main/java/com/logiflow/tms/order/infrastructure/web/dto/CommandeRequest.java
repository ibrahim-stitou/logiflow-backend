package com.logiflow.tms.order.infrastructure.web.dto;

import com.logiflow.tms.shared.domain.vo.Money;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/** Requête de création d'une commande. */
public record CommandeRequest(
    @NotNull UUID clientId,
    @NotNull @FutureOrPresent LocalDate dateSouhaitee,
    @NotNull Money prixNegocie) {}
