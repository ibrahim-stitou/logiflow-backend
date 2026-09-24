package com.logiflow.tms.ai.application.command;

import java.time.Instant;

/**
 * Demande de propositions de voyages à l'agent de planification. {@code typeVoyage} et {@code
 * portee} sont des noms d'énumérations du module planning (SIMPLE, GROUPAGE… ; NATIONAL,
 * INTERNATIONAL).
 */
public record ProposerVoyagesCommand(
    Instant debut, Instant fin, String typeVoyage, String portee, int nbOptions) {}
