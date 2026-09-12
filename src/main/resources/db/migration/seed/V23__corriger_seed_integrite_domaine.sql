-- Corrige des enregistrements V21 qui cassent le mapping domaine (listes API en 400).

-- Plaque SIV invalide (chr(91) = '[' pour GP-022-V[).
UPDATE fleet.vehicule
SET immatriculation = 'GP-022-VZ',
    updated_at = now(),
    updated_by = 'system'
WHERE immatriculation = 'GP-022-V[';

-- Commandes sans ligne de marchandise (le domaine refuse une commande vide).
INSERT INTO commande.ligne_commande (id, commande_id, marchandise_id, poids_kg, volume_m3, nb_colis,
    created_at, created_by, updated_at, updated_by, version)
SELECT
    ('99999992-0000-4000-8000-' || lpad(c.id_num::text, 12, '0'))::uuid,
    ('99999990-0000-4000-8000-' || lpad(c.id_num::text, 12, '0'))::uuid,
    ('66666660-0000-4000-8000-000000000001')::uuid,
    250,
    1.5,
    3,
    now(), 'system', now(), 'system', 0
FROM (
    SELECT c2.i AS id_num
    FROM generate_series(1, 40) AS c2(i)
    WHERE NOT EXISTS (
        SELECT 1
        FROM commande.ligne_commande lc
        WHERE lc.commande_id = ('99999990-0000-4000-8000-' || lpad(c2.i::text, 12, '0'))::uuid
    )
) AS c;

-- Voyages sans dossier attaché (le domaine refuse un voyage vide).
UPDATE planning.voyage v
SET dossier_ids_json = json_build_array(
        ('aaaaaaa0-0000-4000-8000-' || lpad(dossier_num::text, 12, '0'))
    )::text,
    updated_at = now(),
    updated_by = 'system'
FROM (
    SELECT
        substring(reference FROM 'VOY-2026-([0-9]+)')::integer AS voyage_num,
        substring(reference FROM 'VOY-2026-([0-9]+)')::integer AS dossier_num
    FROM planning.voyage
    WHERE dossier_ids_json = '[]'
) AS mapping
WHERE v.reference = 'VOY-2026-' || lpad(mapping.voyage_num::text, 6, '0');
