package com.logiflow.tms.document.domain.model;

/**
 * Nature du document : pièce administrative ou simple photo. Les pièces chauffeur (permis, carte
 * conducteur…) sont toutes optionnelles : leur date d'expiration, si renseignée, est contrôlée.
 */
public enum TypeDocument {
  CARTE_GRISE,
  ASSURANCE,
  CONTROLE_TECHNIQUE,
  ADR,
  PHOTO,
  JUSTIFICATIF_CARBURANT,
  PERMIS_CONDUIRE,
  CARTE_CONDUCTEUR,
  FIMO_FCO,
  VISITE_MEDICALE,
  PIECE_IDENTITE,
  PASSEPORT,
  VISA,
  AUTRE
}
