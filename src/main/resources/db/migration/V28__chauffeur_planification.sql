-- Chauffeur : informations utiles à la planification des voyages.
--  * site_rattachement_id : site du référentiel où le chauffeur est basé (proposer le plus proche
--    du premier chargement). Pas de clé étrangère : le référentiel est un autre module (schéma).
--  * categorie_permis contient désormais une liste de catégories séparées par des virgules
--    (ex. « C,CE »), lue de façon tolérante par CategoriePermis.depuisTexte.
ALTER TABLE driver.chauffeur ADD COLUMN site_rattachement_id uuid;

CREATE INDEX ix_chauffeur_site_rattachement ON driver.chauffeur (site_rattachement_id);
