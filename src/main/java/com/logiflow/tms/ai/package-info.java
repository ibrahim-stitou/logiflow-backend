/**
 * Module IA : façade vers le service externe d'agents IA (application Flask séparée).
 *
 * <p>Aucune logique d'intelligence artificielle ne vit ici : ce module authentifie/autorise,
 * assemble le contexte métier via les {@code api} publiques des autres modules, appelle le service
 * Flask via un adaptateur HTTP interne, journalise l'interaction et renvoie une réponse déjà mise
 * en forme au frontend Angular — qui ne dialogue jamais directement avec Flask. Voir
 * docs/integration-ia.md.
 */
@org.springframework.modulith.ApplicationModule
package com.logiflow.tms.ai;
