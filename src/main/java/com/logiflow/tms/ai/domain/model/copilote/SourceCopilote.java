package com.logiflow.tms.ai.domain.model.copilote;

/**
 * Entité métier citée par une réponse du copilote (lien cliquable côté frontend). {@code type} :
 * DOSSIER, VOYAGE, COMMANDE, VEHICULE, REMORQUE, CHAUFFEUR ou CLIENT ; {@code id} peut être nul.
 */
public record SourceCopilote(String type, String reference, String id) {}
