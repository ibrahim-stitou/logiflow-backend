package com.logiflow.tms.maintenance.application.command;

import com.logiflow.tms.maintenance.domain.model.TypeIntervention;
import com.logiflow.tms.shared.domain.vo.Money;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreerOrdreTravailCommand(
    UUID vehiculeId, TypeIntervention type, LocalDateTime datePlanifiee, Money coutEstime) {}
