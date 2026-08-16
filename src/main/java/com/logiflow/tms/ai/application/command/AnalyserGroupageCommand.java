package com.logiflow.tms.ai.application.command;

import java.util.List;
import java.util.UUID;

/** Commande applicative d'analyse de groupage sur un ensemble de dossiers candidats. */
public record AnalyserGroupageCommand(List<UUID> dossierIds) {}
