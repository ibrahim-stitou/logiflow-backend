package com.logiflow.tms.planning.api.dto;

import java.util.List;

/** Verdict de conformité d'un projet de voyage : anomalies bloquantes et avertissements. */
public record ConformiteVoyageSummary(
    boolean conforme, List<String> bloquants, List<String> avertissements) {}
