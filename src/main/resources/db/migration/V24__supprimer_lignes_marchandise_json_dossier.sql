-- Lignes de marchandise normalisées dans dossier.ligne_marchandise (V20).
-- La colonne JSON héritée de V11 n'est plus alimentée par JPA et bloquait les INSERT (NOT NULL).
ALTER TABLE dossier.dossier_transport
    DROP COLUMN IF EXISTS lignes_marchandise_json;
