package com.logiflow.tms.maintenance.domain.vo;

import com.logiflow.tms.maintenance.domain.model.EtatEcheance;
import java.time.LocalDate;

/**
 * Prochaine échéance d'un plan d'entretien. {@code kmRestant}, {@code heuresRestantes} et {@code
 * dateEcheance} sont null quand la périodicité correspondante n'est pas définie.
 */
public record Echeance(
    Integer kmRestant, Integer heuresRestantes, LocalDate dateEcheance, EtatEcheance etat) {}
