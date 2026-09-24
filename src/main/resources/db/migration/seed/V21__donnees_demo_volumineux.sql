-- Jeu de données volumineux (Maroc, corridors UE / Afrique, devise MAD).
-- Complète V5 (1 client, 1 site Casablanca). Profils local/dev uniquement.
-- Après modification : `make db-reset` puis redémarrer le backend.

-- ---------------------------------------------------------------------------
-- Clients supplémentaires
-- ---------------------------------------------------------------------------
INSERT INTO referential.client (id, code, raison_sociale, actif,
    created_at, created_by, updated_at, updated_by, version)
SELECT
    ('11111111-1111-1111-1111-' || lpad(i::text, 12, '0'))::uuid,
    'CLI-DEMO-' || lpad(i::text, 3, '0'),
    CASE i
        WHEN 2 THEN 'Atlantic Transit Maroc SAS'
        WHEN 3 THEN 'Fret Tanger Méditerranée SA'
        WHEN 4 THEN 'Sahara Distribution SARL'
        ELSE 'Atlas Transport & Logistique'
    END,
    i <> 5,
    now(), 'system', now(), 'system', 0
FROM generate_series(2, 5) AS i;

-- ---------------------------------------------------------------------------
-- Sites (34 nouveaux + 1 hub Casablanca V5 = 35 au total ; MA + corridor UE + Afrique)
-- ---------------------------------------------------------------------------
INSERT INTO referential.site (id, code, libelle, client_id, localisation,
    adresse, horaires_json, contraintes_acces_json, actif,
    created_at, created_by, updated_at, updated_by, version)
SELECT
    ('33333330-0000-4000-8000-' || lpad(i::text, 12, '0'))::uuid,
    'SITE-DEMO-' || lpad(i::text, 3, '0'),
    villes.libelle,
    CASE ((i - 1) % 5)
        WHEN 0 THEN '11111111-1111-1111-1111-111111111111'::uuid
        ELSE ('11111111-1111-1111-1111-' || lpad(((i - 1) % 5 + 1)::text, 12, '0'))::uuid
    END,
    ST_SetSRID(ST_MakePoint(villes.lng, villes.lat), 4326)::geography,
    villes.adresse,
    '[]',
    CASE WHEN i % 9 = 0 THEN '{"interditPoidsLourd":true}' ELSE '{"interditPoidsLourd":false}' END,
    i % 11 <> 0,
    now(), 'system', now(), 'system', 0
FROM generate_series(1, 34) AS i
CROSS JOIN LATERAL (
    SELECT *
    FROM (VALUES
        (1,  'Plateforme Casablanca Ain Sebaa',  -7.5898, 33.5731, 'Zone industrielle Ain Sebaa, Casablanca 20250'),
        (2,  'Terminal Tanger Med',              -5.5470, 35.8694, 'Port Tanger Med, Ksar Sghir 90000'),
        (3,  'Entrepôt Rabat Agdal',             -6.8498, 33.9716, 'Avenue Annakhil, Rabat 10000'),
        (4,  'Hub Fès Saïss',                    -5.0078, 34.0181, 'Route de l''Aéroport, Fès 30000'),
        (5,  'Dépôt Marrakech',                  -8.0089, 31.6295, 'Route de Safi, Marrakech 40000'),
        (6,  'Site Agadir port',                 -9.5981, 30.4278, 'Zone portuaire, Agadir 80000'),
        (7,  'Plateforme Oujda',                 -1.9076, 34.6867, 'Boulevard Mohammed V, Oujda 60000'),
        (8,  'Entrepôt Nador',                   -2.9273, 35.1688, 'Zone franche Nador West Med'),
        (9,  'Hub Kenitra',                      -6.5802, 34.2610, 'Parc logistique Mehdia, Kenitra'),
        (10, 'Dépôt Safi',                       -9.2372, 32.2994, 'Zone industrielle OCP, Safi'),
        (11, 'Site Laâyoune',                   -13.1994, 27.1536, 'Boulevard de la Mecque, Laâyoune'),
        (12, 'Terminal Algeciras',               -5.4565, 36.1408, 'Puerto Bahía de Algeciras, España'),
        (13, 'Hub Madrid Coslada',               -3.7038, 40.4168, 'Polígono Industrial, Coslada 28820'),
        (14, 'Plateforme Barcelona',              2.1734, 41.3851, 'Zona Franca, Barcelona 08040'),
        (15, 'Entrepôt Marseille Fos',            5.3698, 43.2965, 'Port de Fos-sur-Mer, 13270'),
        (16, 'Dépôt Alger Rouiba',                3.0588, 36.7538, 'Zone industrielle Rouiba, Alger'),
        (17, 'Hub Oran Es Sénia',                -0.6417, 35.6971, 'Zone logistique Es Sénia, Oran')
    ) AS t(idx, libelle, lng, lat, adresse)
    WHERE t.idx = ((i - 1) % 17) + 1
) AS villes;

-- ---------------------------------------------------------------------------
-- Marchandises référentielles
-- ---------------------------------------------------------------------------
INSERT INTO referential.marchandise (id, code, libelle, famille, classe_adr, numero_onu,
    gerbable, actif, created_at, created_by, updated_at, updated_by, version)
SELECT
    ('66666660-0000-4000-8000-' || lpad(i::text, 12, '0'))::uuid,
    'MCH-DEMO-' || lpad(i::text, 3, '0'),
    libelles.libelle,
    libelles.famille,
    libelles.classe_adr,
    libelles.numero_onu,
    i % 4 <> 0,
    i % 13 <> 0,
    now(), 'system', now(), 'system', 0
FROM generate_series(1, 12) AS i
CROSS JOIN LATERAL (
    SELECT *
    FROM (VALUES
        (1,  'Agrumes export (caisses)',  'Agroalimentaire', NULL,  NULL),
        (2,  'Pièces auto assemblage',    'Automobile',      NULL,  NULL),
        (3,  'Phosphate granulé',        'Mines',           NULL,  NULL),
        (4,  'Produits chimiques',        'Chimie',          '3',   '1170'),
        (5,  'Textile confection',        'Textile',         NULL,  NULL),
        (6,  'Équipements électriques',   'Industrie',       NULL,  NULL),
        (7,  'Ciment et agrégats',        'BTP',             NULL,  NULL),
        (8,  'Produits frais réfrigérés', 'Agroalimentaire', NULL,  NULL),
        (9,  'Emballages plastiques',     'Emballage',       NULL,  NULL),
        (10, 'Matériel médical',          'Santé',           NULL,  NULL),
        (11, 'Colis e-commerce',          'Distribution',    NULL,  NULL),
        (12, 'Huile d''olive vrac',       'Agroalimentaire', NULL,  NULL)
    ) AS t(idx, libelle, famille, classe_adr, numero_onu)
    WHERE t.idx = i
) AS libelles;

-- ---------------------------------------------------------------------------
-- Véhicules (35)
-- ---------------------------------------------------------------------------
INSERT INTO fleet.vehicule (id, immatriculation, type, numero_parc, marque, modele,
    ptac_kg, poids_vide_kg, charge_utile_kg, kilometrage, heures_moteur, statut,
    groupe_froid, created_at, created_by, updated_at, updated_by, version)
SELECT
    ('44444440-0000-4000-8000-' || lpad(i::text, 12, '0'))::uuid,
    (substring('MATGCBRS', 1 + ((i - 1) % 8), 1) || substring('ACDEFGHK', 1 + ((i + 2) % 8), 1))
        || '-' || lpad(i::text, 3, '0') || '-'
        || (substring('ABCDEFGHJKLMNPRSTUVWXYZ', 1 + ((i * 3 - 1) % 23), 1)
            || substring('ABCDEFGHJKLMNPRSTUVWXYZ', 1 + ((i * 5 - 1) % 23), 1)),
    (ARRAY['TRACTEUR', 'PORTEUR', 'FOURGON'])[1 + ((i - 1) % 3)],
    'PARC-' || lpad(i::text, 4, '0'),
    (ARRAY['Renault', 'Volvo', 'Mercedes', 'MAN', 'DAF'])[1 + ((i - 1) % 5)],
    (ARRAY['T High', 'FH', 'Actros', 'TGX', 'XF'])[1 + ((i - 1) % 5)],
    26000 + (i % 5) * 2000,
    7500 + (i % 7) * 200,
    12000 + (i % 4) * 500,
    50000 + i * 1370,
    2000 + i * 17,
    (ARRAY[
        'DISPONIBLE', 'DISPONIBLE', 'DISPONIBLE',
        'EN_VOYAGE', 'EN_VOYAGE', 'EN_VOYAGE',
        'EN_MAINTENANCE', 'EN_MAINTENANCE',
        'IMMOBILISE', 'HORS_SERVICE',
        'RESERVE', 'DISPONIBLE'
    ])[1 + ((i - 1) % 12)],
    i % 6 = 0,
    now(), 'system', now(), 'system', 0
FROM generate_series(1, 35) AS i;

-- ---------------------------------------------------------------------------
-- Remorques (15)
-- ---------------------------------------------------------------------------
INSERT INTO fleet.remorque (id, immatriculation, type, carrosserie, numero_parc,
    volume_utile_m3, nb_positions_palettes, charge_utile_kg, kilometrage,
    heures_groupe_froid, statut, groupe_froid,
    created_at, created_by, updated_at, updated_by, version)
SELECT
    ('77777770-0000-4000-8000-' || lpad(i::text, 12, '0'))::uuid,
    (substring('RTFGHKLM', 1 + ((i - 1) % 8), 1) || substring('ACDEFGHK', 1 + ((i + 1) % 8), 1))
        || '-' || lpad(i::text, 3, '0') || '-'
        || (substring('ABCDEFGHJKLMNPRSTUVWXYZ', 1 + ((i * 2 - 1) % 23), 1)
            || substring('ABCDEFGHJKLMNPRSTUVWXYZ', 1 + ((i * 7 - 1) % 23), 1)),
    'SEMI_REMORQUE',
    (ARRAY['TAUTLINER', 'FRIGORIFIQUE', 'PLATEAU', 'BENNE', 'PORTE_CONTENEUR'])[1 + ((i - 1) % 5)],
    'REM-' || lpad(i::text, 4, '0'),
    80 + (i % 5) * 4,
    30 + (i % 4) * 2,
    22000 + (i % 3) * 1000,
    30000 + i * 890,
    i % 3 * 120,
    (ARRAY['DISPONIBLE', 'EN_VOYAGE', 'EN_MAINTENANCE', 'RESERVE'])[1 + ((i - 1) % 4)],
    i % 3 = 0,
    now(), 'system', now(), 'system', 0
FROM generate_series(1, 15) AS i;

-- ---------------------------------------------------------------------------
-- Chauffeurs (12)
-- ---------------------------------------------------------------------------
INSERT INTO driver.chauffeur (id, matricule, nom, prenom, telephone, email,
    statut, disponibilite, solde_temps_conduite_minutes,
    created_at, created_by, updated_at, updated_by, version)
SELECT
    ('55555550-0000-4000-8000-' || lpad(i::text, 12, '0'))::uuid,
    'DRV-' || lpad(i::text, 4, '0'),
    (ARRAY['Alami', 'Benjelloun', 'Chraibi', 'Fassi', 'Idrissi', 'Lahlou',
           'Mansouri', 'Naciri', 'Ouazzani', 'Rahmani', 'Saadi', 'Tazi'])[i],
    (ARRAY['Youssef', 'Fatima', 'Karim', 'Amina', 'Omar', 'Salma',
           'Hassan', 'Nadia', 'Mehdi', 'Leila', 'Rachid', 'Sanae'])[i],
    '+212' || lpad((600000000 + i * 123457)::text, 9, '0'),
    lower(
        (ARRAY['youssef', 'fatima', 'karim', 'amina', 'omar', 'salma',
               'hassan', 'nadia', 'mehdi', 'leila', 'rachid', 'sanae'])[i]
    ) || '.chauffeur' || i || '@logiflow.demo',
    'ACTIF',
    (ARRAY['DISPONIBLE', 'EN_VOYAGE', 'EN_REPOS', 'DISPONIBLE'])[1 + ((i - 1) % 4)],
    (i % 5) * 90,
    now(), 'system', now(), 'system', 0
FROM generate_series(1, 12) AS i;

-- ---------------------------------------------------------------------------
-- Utilisateurs IAM (25)
-- ---------------------------------------------------------------------------
INSERT INTO iam.utilisateur (id, login, email, roles_json, actif,
    created_at, created_by, updated_at, updated_by, version)
SELECT
    ('88888880-0000-4000-8000-' || lpad(i::text, 12, '0'))::uuid,
    'user.demo.' || lpad(i::text, 2, '0'),
    'user.demo.' || lpad(i::text, 2, '0') || '@logiflow.tms',
    CASE ((i - 1) % 6)
        WHEN 0 THEN '["ADMINISTRATEUR"]'
        WHEN 1 THEN '["EXPLOITANT"]'
        WHEN 2 THEN '["RESPONSABLE_EXPLOITATION"]'
        WHEN 3 THEN '["ATELIER"]'
        WHEN 4 THEN '["COMMERCIAL"]'
        ELSE '["CHAUFFEUR"]'
    END,
    i % 17 <> 0,
    now(), 'system', now(), 'system', 0
FROM generate_series(1, 25) AS i;

-- ---------------------------------------------------------------------------
-- Commandes (40)
-- ---------------------------------------------------------------------------
INSERT INTO commande.commande (id, reference, client_id, statut, date_souhaitee,
    prix_montant, prix_devise, created_at, created_by, updated_at, updated_by, version)
SELECT
    ('99999990-0000-4000-8000-' || lpad(i::text, 12, '0'))::uuid,
    'CMD-2026-' || lpad(i::text, 6, '0'),
    CASE ((i - 1) % 5)
        WHEN 0 THEN '11111111-1111-1111-1111-111111111111'::uuid
        ELSE ('11111111-1111-1111-1111-' || lpad(((i - 1) % 5 + 1)::text, 12, '0'))::uuid
    END,
    CASE
        WHEN i <= 8 THEN 'RECUE'
        WHEN i <= 35 THEN 'CONFIRMEE'
        ELSE 'ANNULEE'
    END,
    (date '2026-09-01' + ((i - 1) % 28))::date,
    1200 + (i * 137.5),
    'MAD',
    now(), 'system', now(), 'system', 0
FROM generate_series(1, 40) AS i;

-- Lignes de commande pour les commandes confirmées
INSERT INTO commande.ligne_commande (id, commande_id, marchandise_id, poids_kg, volume_m3, nb_colis,
    created_at, created_by, updated_at, updated_by, version)
SELECT
    ('99999991-0000-4000-8000-' || lpad(i::text, 12, '0'))::uuid,
    ('99999990-0000-4000-8000-' || lpad(i::text, 12, '0'))::uuid,
    ('66666660-0000-4000-8000-' || lpad(((i - 1) % 12 + 1)::text, 12, '0'))::uuid,
    400 + (i % 9) * 50,
    2 + (i % 5) * 0.5,
    5 + (i % 15),
    now(), 'system', now(), 'system', 0
FROM generate_series(1, 40) AS i;

-- ---------------------------------------------------------------------------
-- Dossiers de transport (35) + lignes marchandise
-- ---------------------------------------------------------------------------
DO $$
DECLARE
    i integer;
    statut text;
    site_ch_idx integer;
    site_de_idx integer;
    site_ch uuid;
    site_de uuid;
    site_tanger uuid := '33333330-0000-4000-8000-000000000002'::uuid;
    site_algeciras uuid := '33333330-0000-4000-8000-000000000012'::uuid;
    depart timestamptz;
    arrivee timestamptz;
    segments jsonb;
    ordre_seg integer;
    statuts text[] := ARRAY[
        'CREE', 'CREE', 'CREE', 'CREE', 'CREE', 'CREE', 'CREE', 'CREE',
        'PLANIFIE', 'PLANIFIE', 'PLANIFIE', 'PLANIFIE', 'PLANIFIE', 'PLANIFIE',
        'EN_CHARGEMENT', 'CHARGE',
        'EN_TRANSIT', 'EN_TRANSIT', 'EN_TRANSIT', 'EN_TRANSIT',
        'EN_LIVRAISON', 'EN_LIVRAISON',
        'LIVRE', 'LIVRE', 'LIVRE',
        'CLOTURE', 'CLOTURE',
        'INCIDENT', 'INCIDENT', 'INCIDENT',
        'ANNULE', 'ANNULE',
        'CREE', 'PLANIFIE', 'EN_TRANSIT'
    ];
BEGIN
    FOR i IN 1..35 LOOP
        statut := statuts[i];
        IF i <= 20 THEN
            site_ch_idx := ((i - 1) % 11) + 1;
            site_de_idx := (i % 11) + 1;
        ELSIF i <= 28 THEN
            site_ch_idx := ((i - 1) % 11) + 1;
            site_de_idx := 11 + ((i - 19) % 4) + 1;
        ELSE
            site_ch_idx := ((i - 1) % 11) + 1;
            site_de_idx := 15 + ((i - 29) % 2) + 1;
        END IF;
        site_ch := ('33333330-0000-4000-8000-' || lpad(site_ch_idx::text, 12, '0'))::uuid;
        site_de := ('33333330-0000-4000-8000-' || lpad(site_de_idx::text, 12, '0'))::uuid;
        depart := timestamptz '2026-09-10 06:00:00+00' + ((i - 1) || ' days')::interval;
        arrivee := depart + interval '2 hours';

        -- Corridor MA→Europe : chargement → Tanger Med → Algeciras → déchargement
        -- pour un itinéraire routier OSRM côté Europe + courte traversée ferry.
        segments := jsonb_build_array(
            jsonb_build_object(
                'type', 'CHARGEMENT',
                'ordre', 0,
                'siteId', site_ch::text,
                'fenetre', jsonb_build_object(
                    'debut', to_char(depart at time zone 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS"Z"'),
                    'fin', to_char((depart + interval '2 hours') at time zone 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS"Z"')
                ),
                'realiseLe', CASE
                    WHEN statut IN ('CHARGE', 'EN_TRANSIT', 'EN_LIVRAISON', 'LIVRE', 'CLOTURE')
                    THEN to_char(depart at time zone 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS"Z"')
                    ELSE NULL
                END
            )
        );
        ordre_seg := 1;

        IF i BETWEEN 21 AND 28 THEN
            IF site_ch <> site_tanger THEN
                segments := segments || jsonb_build_array(
                    jsonb_build_object(
                        'type', 'ESCALE',
                        'ordre', ordre_seg,
                        'siteId', site_tanger::text,
                        'fenetre', jsonb_build_object(
                            'debut', to_char((depart + interval '8 hours') at time zone 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS"Z"'),
                            'fin', to_char((depart + interval '10 hours') at time zone 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS"Z"')
                        ),
                        'realiseLe', NULL
                    )
                );
                ordre_seg := ordre_seg + 1;
            END IF;
            IF site_de <> site_algeciras THEN
                segments := segments || jsonb_build_array(
                    jsonb_build_object(
                        'type', 'ESCALE',
                        'ordre', ordre_seg,
                        'siteId', site_algeciras::text,
                        'fenetre', jsonb_build_object(
                            'debut', to_char((depart + interval '14 hours') at time zone 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS"Z"'),
                            'fin', to_char((depart + interval '16 hours') at time zone 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS"Z"')
                        ),
                        'realiseLe', NULL
                    )
                );
                ordre_seg := ordre_seg + 1;
            END IF;
        END IF;

        segments := segments || jsonb_build_array(
            jsonb_build_object(
                'type', 'DECHARGEMENT',
                'ordre', ordre_seg,
                'siteId', site_de::text,
                'fenetre', jsonb_build_object(
                    'debut', to_char((depart + interval '1 day') at time zone 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS"Z"'),
                    'fin', to_char((depart + interval '1 day 2 hours') at time zone 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS"Z"')
                ),
                'realiseLe', CASE
                    WHEN statut IN ('LIVRE', 'CLOTURE')
                    THEN to_char((depart + interval '1 day') at time zone 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS"Z"')
                    ELSE NULL
                END
            )
        );

        INSERT INTO dossier.dossier_transport (
            id, reference, commande_id, statut, type_transport, groupable,
            poids_brut_kg, volume_m3, nb_palettes, famille_marchandise,
            carrosserie_requise, temperature_requise,
            lignes_marchandise_json, segments_json, documents_json,
            created_at, created_by, updated_at, updated_by, version
        ) VALUES (
            ('aaaaaaa0-0000-4000-8000-' || lpad(i::text, 12, '0'))::uuid,
            'DT-2026-' || lpad(i::text, 6, '0'),
            ('99999990-0000-4000-8000-' || lpad(i::text, 12, '0'))::uuid,
            statut,
            CASE
                WHEN i <= 20 THEN 'NATIONAL'
                WHEN i <= 28 AND site_de_idx <= 15 THEN 'EXPORT'
                WHEN i > 28 THEN 'EXPORT'
                ELSE 'TRANSIT'
            END,
            i % 7 <> 0,
            400 + (i % 9) * 50,
            2 + (i % 5) * 0.5,
            5 + (i % 15),
            'Palettes standard',
            CASE WHEN i % 5 = 0 THEN 'FRIGORIFIQUE' ELSE 'TAUTLINER' END,
            CASE WHEN i % 5 = 0 THEN 4.0 ELSE NULL END,
            '[]',
            segments::text,
            '[]',
            now(), 'system', now(), 'system', 0
        );

        INSERT INTO dossier.ligne_marchandise (
            id, dossier_id, marchandise_id, poids_kg, volume_m3, nb_colis,
            gerbable, created_at, created_by, updated_at, updated_by, version
        ) VALUES (
            ('aaaaaaa1-0000-4000-8000-' || lpad(i::text, 12, '0'))::uuid,
            ('aaaaaaa0-0000-4000-8000-' || lpad(i::text, 12, '0'))::uuid,
            ('66666660-0000-4000-8000-' || lpad(((i - 1) % 12 + 1)::text, 12, '0'))::uuid,
            400 + (i % 9) * 50,
            2 + (i % 5) * 0.5,
            5 + (i % 15),
            true,
            now(), 'system', now(), 'system', 0
        );
    END LOOP;
END $$;

-- ---------------------------------------------------------------------------
-- Voyages (30)
-- ---------------------------------------------------------------------------
DO $$
DECLARE
    i integer;
    statut text;
    dossier_id uuid;
    dossier_ids text;
    depart timestamptz;
    arrivee timestamptz;
    trajet text;
    affectations text;
    statuts text[] := ARRAY[
        'BROUILLON', 'BROUILLON', 'BROUILLON',
        'PLANIFIE', 'PLANIFIE', 'PLANIFIE', 'PLANIFIE', 'PLANIFIE',
        'AFFECTE', 'AFFECTE', 'AFFECTE', 'AFFECTE', 'AFFECTE',
        'EN_COURS', 'EN_COURS', 'EN_COURS', 'EN_COURS',
        'TERMINE', 'TERMINE', 'TERMINE', 'TERMINE',
        'CLOTURE', 'CLOTURE', 'CLOTURE',
        'ANNULE', 'ANNULE', 'ANNULE',
        'PLANIFIE', 'AFFECTE', 'EN_COURS'
    ];
BEGIN
    FOR i IN 1..30 LOOP
        statut := statuts[i];
        depart := timestamptz '2026-09-12 05:00:00+00' + ((i - 1) || ' hours')::interval;
        arrivee := depart + interval '8 hours';

        IF i = 28 THEN
            dossier_ids := json_build_array(
                'aaaaaaa0-0000-4000-8000-000000000030',
                'aaaaaaa0-0000-4000-8000-000000000031'
            )::text;
        ELSE
            dossier_id := ('aaaaaaa0-0000-4000-8000-' || lpad((i + 8)::text, 12, '0'))::uuid;
            dossier_ids := json_build_array(dossier_id::text)::text;
        END IF;

        trajet := json_build_object(
            'distanceTotaleKm', 200 + (i * 17.5),
            'dureeConduiteMin', 180 + (i * 5),
            'dureeTotaleMin', 240 + (i * 5),
            'etapes', json_build_array(
                json_build_object(
                    'ordre', 0,
                    'type', 'CHARGEMENT',
                    'eta', to_char(depart at time zone 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS"Z"'),
                    'etd', to_char((depart + interval '1 hour') at time zone 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS"Z"'),
                    'distanceDepuisPrecedenteKm', 0,
                    'chargeApresKg', 500 + (i * 10)
                ),
                json_build_object(
                    'ordre', 1,
                    'type', 'DECHARGEMENT',
                    'eta', to_char(arrivee at time zone 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS"Z"'),
                    'etd', NULL,
                    'distanceDepuisPrecedenteKm', 200 + (i * 17.5),
                    'chargeApresKg', 0
                )
            )
        )::text;

        affectations := json_build_array(
            json_build_object(
                'chauffeurId', ('55555550-0000-4000-8000-' || lpad(((i - 1) % 12 + 1)::text, 12, '0')),
                'role', CASE WHEN i % 7 = 0 THEN 'RENFORT' ELSE 'TITULAIRE' END,
                'dateAffectation', to_char((depart - interval '1 day') at time zone 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS"Z"')
            )
        )::text;

        INSERT INTO planning.voyage (
            id, reference, type_voyage, portee, statut,
            depart_prevu, arrivee_prevue,
            vehicule_id, remorque_id,
            dossier_ids_json, trajet_json, affectations_json, taux_remplissage,
            created_at, created_by, updated_at, updated_by, version
        ) VALUES (
            ('bbbbbbb0-0000-4000-8000-' || lpad(i::text, 12, '0'))::uuid,
            'VOY-2026-' || lpad(i::text, 6, '0'),
            (ARRAY['SIMPLE', 'GROUPAGE', 'RAMASSE', 'DISTRIBUTION', 'NAVETTE'])[1 + ((i - 1) % 5)],
            CASE
                WHEN i % 6 = 0 OR i IN (8, 14, 20, 26) THEN 'INTERNATIONAL'
                ELSE 'NATIONAL'
            END,
            statut,
            depart,
            arrivee,
            ('44444440-0000-4000-8000-' || lpad(((i - 1) % 35 + 1)::text, 12, '0'))::uuid,
            CASE
                WHEN i % 4 = 0 THEN NULL
                ELSE ('77777770-0000-4000-8000-' || lpad(((i - 1) % 15 + 1)::text, 12, '0'))::uuid
            END,
            dossier_ids,
            trajet,
            affectations,
            0.45 + ((i % 10) * 0.05),
            now(), 'system', now(), 'system', 0
        );
    END LOOP;
END $$;

-- ---------------------------------------------------------------------------
-- Maintenance : ordres de travail (30)
-- ---------------------------------------------------------------------------
INSERT INTO maintenance.ordre_travail (id, vehicule_id, type_intervention, statut,
    date_planifiee, duree_reelle_min, cout_montant, cout_devise,
    created_at, created_by, updated_at, updated_by, version)
SELECT
    ('ccccccc0-0000-4000-8000-' || lpad(i::text, 12, '0'))::uuid,
    ('44444440-0000-4000-8000-' || lpad(((i - 1) % 35 + 1)::text, 12, '0'))::uuid,
    (ARRAY[
        'ENTRETIEN_PREVENTIF', 'REPARATION', 'CONTROLE_TECHNIQUE', 'PNEUS', 'AUTRE'
    ])[1 + ((i - 1) % 5)],
    (ARRAY['PLANIFIE', 'PLANIFIE', 'EN_COURS', 'EN_COURS', 'TERMINE', 'TERMINE', 'ANNULE'])[1 + ((i - 1) % 7)],
    timestamp '2026-09-15 08:00:00' + ((i - 1) || ' days')::interval,
    CASE WHEN i % 3 = 0 THEN 90 + (i % 5) * 30 ELSE 0 END,
    150 + (i * 42.5),
    'MAD',
    now(), 'system', now(), 'system', 0
FROM generate_series(1, 30) AS i;

-- Plans d'entretien et scores santé (échantillon pour la flotte)
INSERT INTO maintenance.plan_entretien (id, vehicule_id, libelle, periodicite_km,
    periodicite_mois, seuil_alerte_km, duree_estimee_min,
    created_at, created_by, updated_at, updated_by, version)
SELECT
    ('ccccccc1-0000-4000-8000-' || lpad(i::text, 12, '0'))::uuid,
    ('44444440-0000-4000-8000-' || lpad(i::text, 12, '0'))::uuid,
    'Révision périodique',
    40000,
    12,
    2000,
    120,
    now(), 'system', now(), 'system', 0
FROM generate_series(1, 10) AS i;

INSERT INTO maintenance.score_sante (id, vehicule_id, calcule_le, score, statut,
    km_avant_echeance, date_echeance_projetee, recommandation,
    created_at, created_by, updated_at, updated_by, version)
SELECT
    ('ccccccc2-0000-4000-8000-' || lpad(i::text, 12, '0'))::uuid,
    ('44444440-0000-4000-8000-' || lpad(i::text, 12, '0'))::uuid,
    (date '2026-09-01' + ((i - 1) % 10))::date,
    55 + (i % 40),
    (ARRAY['BON', 'SURVEILLER', 'A_PLANIFIER', 'CRITIQUE'])[1 + ((i - 1) % 4)],
    5000 - (i * 120),
    (date '2026-10-01' + ((i - 1) % 20))::date,
    CASE
        WHEN i % 3 = 0 THEN 'Planifier un entretien préventif sous 15 jours.'
        ELSE NULL
    END,
    now(), 'system', now(), 'system', 0
FROM generate_series(1, 10) AS i;
