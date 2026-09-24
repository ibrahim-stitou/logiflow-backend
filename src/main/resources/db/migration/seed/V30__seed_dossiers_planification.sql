-- Dossiers CREE à planifier dans les prochains jours, pour démontrer l'agent de planification :
-- zones regroupables (Rhône → Sud, Nord → Est), 2 dossiers ADR, 3 frigorifiques à 4 °C et un
-- dossier lourd non groupable. Les dates sont relatives à la date d'application de la migration
-- (J+3 et J+4 à partir de 06:00 UTC).
--
-- Sites V21 : 1 Paris, 2 Lyon, 3 Marseille, 4 Toulouse, 5 Nice, 6 Nantes, 7 Strasbourg,
-- 8 Montpellier, 9 Bordeaux, 10 Lille, 12 Dijon, 13 Grenoble, 14 Angers, 15 Le Havre, 16 Reims,
-- 17 Tours. Marchandises : 1 palettes alimentaires, 4 produits chimiques (ADR 3), 8 produits frais.

-- Remorques frigorifiques utilisables : groupe froid et plage de température cohérents.
UPDATE fleet.remorque
SET groupe_froid = true,
    temperature_min = -25.0,
    temperature_max = 12.0,
    statut = CASE WHEN statut IN ('EN_MAINTENANCE', 'IMMOBILISE') THEN 'DISPONIBLE' ELSE statut END,
    updated_at = now(),
    updated_by = 'system'
WHERE carrosserie = 'FRIGORIFIQUE';

WITH base AS (
    SELECT date_trunc('day', now() AT TIME ZONE 'UTC') AT TIME ZONE 'UTC'
           + interval '3 days 6 hours' AS j0
),
dossiers (n, site_ch, site_de, h_ch, h_de, poids, volume, palettes, groupable,
          marchandise, carrosserie, temperature) AS (
    VALUES
        -- Rhône-Alpes / Bourgogne → Sud
        (1,  2,  3,  0, 6,  6000, 24.0, 12, true,  1, 'TAUTLINER',    NULL::double precision),
        (2,  2,  8,  1, 7,  4000, 16.0,  8, true,  1, 'TAUTLINER',    NULL),
        (3,  13, 3,  0, 6,  5000, 20.0, 10, true,  1, NULL,           NULL),
        (4,  2,  5,  2, 8,  3000, 12.0,  6, true,  1, 'TAUTLINER',    NULL),
        (5,  12, 2,  0, 4,  7000, 28.0, 14, true,  1, 'TAUTLINER',    NULL),
        -- Nord / Île-de-France → Est
        (6,  1,  10, 0, 4,  8000, 30.0, 16, true,  1, 'TAUTLINER',    NULL),
        (7,  1,  16, 1, 4,  5000, 20.0, 10, true,  1, 'TAUTLINER',    NULL),
        (8,  10, 16, 5, 9,  3000, 12.0,  6, true,  1, NULL,           NULL),
        (9,  1,  7,  24, 32, 9000, 34.0, 18, true, 1, 'TAUTLINER',    NULL),
        (10, 16, 7,  26, 31, 4000, 16.0,  8, true, 1, 'TAUTLINER',    NULL),
        -- Matières dangereuses (ADR)
        (11, 2,  3,  1, 7,  5000, 18.0, 10, true,  4, 'TAUTLINER',    NULL),
        (12, 4,  9,  0, 5,  6000, 22.0, 12, true,  4, 'TAUTLINER',    NULL),
        -- Frigorifique à 4 °C
        (13, 6,  14, 0, 3,  5000, 20.0, 10, true,  8, 'FRIGORIFIQUE', 4.0),
        (14, 6,  17, 1, 5,  3000, 12.0,  6, true,  8, 'FRIGORIFIQUE', 4.0),
        (15, 9,  4,  24, 29, 4000, 16.0, 8, true,  8, 'FRIGORIFIQUE', 4.0),
        -- Chargement complet, non groupable
        (16, 15, 1,  2, 7,  22000, 80.0, 33, false, 1, 'TAUTLINER',   NULL)
),
calcul AS (
    SELECT d.*,
        ('aaaaaab0-0000-4000-8000-' || lpad(d.n::text, 12, '0'))::uuid AS dossier_id,
        ('33333330-0000-4000-8000-' || lpad(d.site_ch::text, 12, '0')) AS site_ch_id,
        ('33333330-0000-4000-8000-' || lpad(d.site_de::text, 12, '0')) AS site_de_id,
        b.j0 + make_interval(hours => d.h_ch) AS debut_ch,
        b.j0 + make_interval(hours => d.h_de) AS debut_de
    FROM dossiers d CROSS JOIN base b
),
insertion AS (
    INSERT INTO dossier.dossier_transport (
        id, reference, commande_id, statut, type_transport, groupable,
        poids_brut_kg, volume_m3, nb_palettes, famille_marchandise,
        carrosserie_requise, temperature_requise,
        segments_json, documents_json,
        created_at, created_by, updated_at, updated_by, version
    )
    SELECT
        c.dossier_id,
        'DT-2026-9' || lpad(c.n::text, 5, '0'),
        ('99999990-0000-4000-8000-' || lpad(c.n::text, 12, '0'))::uuid,
        'CREE',
        'NATIONAL',
        c.groupable,
        c.poids,
        c.volume,
        c.palettes,
        CASE c.marchandise WHEN 4 THEN 'Produits chimiques' WHEN 8 THEN 'Produits frais'
            ELSE 'Palettes standard' END,
        c.carrosserie,
        c.temperature,
        json_build_array(
            json_build_object(
                'type', 'CHARGEMENT', 'ordre', 0, 'siteId', c.site_ch_id,
                'fenetre', json_build_object(
                    'debut', to_char(c.debut_ch AT TIME ZONE 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS"Z"'),
                    'fin', to_char((c.debut_ch + interval '4 hours') AT TIME ZONE 'UTC',
                                   'YYYY-MM-DD"T"HH24:MI:SS"Z"')),
                'realiseLe', NULL),
            json_build_object(
                'type', 'DECHARGEMENT', 'ordre', 1, 'siteId', c.site_de_id,
                'fenetre', json_build_object(
                    'debut', to_char(c.debut_de AT TIME ZONE 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS"Z"'),
                    'fin', to_char((c.debut_de + interval '8 hours') AT TIME ZONE 'UTC',
                                   'YYYY-MM-DD"T"HH24:MI:SS"Z"')),
                'realiseLe', NULL)
        )::text,
        '[]',
        now(), 'system', now(), 'system', 0
    FROM calcul c
    RETURNING id
)
INSERT INTO dossier.ligne_marchandise (
    id, dossier_id, marchandise_id, poids_kg, volume_m3, nb_colis,
    classe_adr, numero_onu, gerbable,
    created_at, created_by, updated_at, updated_by, version
)
SELECT
    ('aaaaaab1-0000-4000-8000-' || lpad(c.n::text, 12, '0'))::uuid,
    c.dossier_id,
    ('66666660-0000-4000-8000-' || lpad(c.marchandise::text, 12, '0'))::uuid,
    c.poids,
    c.volume,
    c.palettes,
    CASE WHEN c.marchandise = 4 THEN '3' END,
    CASE WHEN c.marchandise = 4 THEN '1170' END,
    true,
    now(), 'system', now(), 'system', 0
FROM calcul c
JOIN insertion i ON i.id = c.dossier_id;
