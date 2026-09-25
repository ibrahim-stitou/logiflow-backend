package com.logiflow.tms.maintenance.api;

import java.util.UUID;

/**
 * Publié après un changement qui modifie l'état de maintenance d'un engin : clôture d'un ordre de
 * travail, déclaration d'un sinistre, sinistre qui immobilise ou libère l'engin. Permet à l'agent
 * de maintenance prédictive de recalculer son score sans attendre l'analyse de nuit.
 *
 * @param typeEngin VEHICULE ou REMORQUE
 * @param motif libellé lisible (« Clôture de l'OT OT-2026-000012 »)
 */
public record EtatMaintenanceEnginModifieEvent(String typeEngin, UUID enginId, String motif) {}
