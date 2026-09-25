-- Données de démonstration du module maintenance refondu (V31) : prestataires, contrats
-- d'assurance, plans d'entretien (dernières réalisations variées : échus, en alerte, OK), un an
-- d'ordres de travail avec lignes de coût, et sinistres à différents stades.
--
-- Cohérence avec la flotte V21 : les engins EN_MAINTENANCE reçoivent un OT en cours, les véhicules
-- IMMOBILISE un sinistre ouvert qui les immobilise. Dates relatives à l'application du seed.

-- ---------------------------------------------------------------------------
-- Prestataires
-- ---------------------------------------------------------------------------
INSERT INTO maintenance.prestataire (id, code, raison_sociale, type_prestataire, siret,
    contact_nom, telephone, email, adresse, notes, actif,
    created_at, created_by, updated_at, updated_by, version)
VALUES
    ('dddddd00-0000-4000-8000-000000000001', 'GAR-PL-LYON', 'Garage Poids Lourds Lyonnais', 'GARAGE', '41234567800011',
     'Marc Girard', '+33478000001', 'atelier@gpl-lyon.demo', '12 rue de l''Industrie, 69200 Vénissieux', 'Atelier principal, contrat d''entretien.', true, now(), 'system', now(), 'system', 0),
    ('dddddd00-0000-4000-8000-000000000002', 'CONC-RT-69', 'Renault Trucks Rhône', 'CONCESSION', '41234567800029',
     'Service après-vente', '+33478000002', 'sav@rt-rhone.demo', '5 avenue des Poids Lourds, 69800 Saint-Priest', NULL, true, now(), 'system', now(), 'system', 0),
    ('dddddd00-0000-4000-8000-000000000003', 'PNEU-EURO', 'Euro Pneus Poids Lourds', 'PNEUMATIQUES', '41234567800037',
     'Julie Martin', '+33478000003', 'contact@europneus.demo', '8 route de Grenoble, 69800 Saint-Priest', 'Intervention sur site possible.', true, now(), 'system', now(), 'system', 0),
    ('dddddd00-0000-4000-8000-000000000004', 'CARR-RHONE', 'Carrosserie Industrielle du Rhône', 'CARROSSERIE', '41234567800045',
     'Pierre Faure', '+33478000004', 'devis@cir.demo', '3 chemin des Carrossiers, 69740 Genas', NULL, true, now(), 'system', now(), 'system', 0),
    ('dddddd00-0000-4000-8000-000000000005', 'CT-DEKRA-69', 'Centre de contrôle technique PL Est Lyonnais', 'CONTROLE_TECHNIQUE', '41234567800052',
     'Accueil', '+33478000005', 'rdv@ct-pl.demo', '20 rue du Contrôle, 69150 Décines', NULL, true, now(), 'system', now(), 'system', 0),
    ('dddddd00-0000-4000-8000-000000000006', 'DEP-24H', 'Dépannage Autoroutier 24/7', 'DEPANNAGE', '41234567800060',
     'Permanence', '+33800000006', 'permanence@dep247.demo', 'A7 — aire de Solaize', NULL, true, now(), 'system', now(), 'system', 0),
    ('dddddd00-0000-4000-8000-000000000007', 'EXP-AUTO', 'Cabinet d''Expertise Automobile Lemoine', 'EXPERT', '41234567800078',
     'Sophie Lemoine', '+33478000007', 'expertise@lemoine.demo', '1 place Bellecour, 69002 Lyon', NULL, true, now(), 'system', now(), 'system', 0),
    ('dddddd00-0000-4000-8000-000000000008', 'ASS-MUTTRANS', 'Mutuelle des Transporteurs', 'ASSUREUR', '41234567800086',
     'Gestion sinistres flotte', '+33140000008', 'sinistres@mutrans.demo', '40 boulevard Haussmann, 75009 Paris', 'Déclaration sous 5 jours ouvrés.', true, now(), 'system', now(), 'system', 0),
    ('dddddd00-0000-4000-8000-000000000009', 'ASS-FROID', 'Assurances Transports Frigorifiques', 'ASSUREUR', '41234567800094',
     'Service marchandises', '+33140000009', 'contact@atf.demo', '15 rue de la Chaîne du Froid, 94150 Rungis', NULL, true, now(), 'system', now(), 'system', 0);

-- ---------------------------------------------------------------------------
-- Contrats d'assurance : flotte (tous engins) + remorques frigorifiques (marchandises)
-- ---------------------------------------------------------------------------
INSERT INTO maintenance.contrat_assurance (id, assureur_id, numero_police, type_contrat,
    garanties_json, franchise, prime_annuelle, date_effet, date_echeance, engins_json, actif,
    created_at, created_by, updated_at, updated_by, version)
SELECT
    'dddddd01-0000-4000-8000-000000000001'::uuid, 'dddddd00-0000-4000-8000-000000000008'::uuid, 'MT-FLOTTE-2026-0457', 'FLOTTE',
    '["RC","DOMMAGES","VOL","INCENDIE","BRIS_GLACE","ASSISTANCE"]', 10000.00, 865000.00,
    date_trunc('year', now())::date, (date_trunc('year', now()) + interval '1 year - 1 day')::date, '[]', true,
    now(), 'system', now(), 'system', 0
UNION ALL
SELECT
    'dddddd01-0000-4000-8000-000000000002'::uuid, 'dddddd00-0000-4000-8000-000000000009'::uuid, 'ATF-FRIGO-2026-0112', 'ENGIN',
    '["DOMMAGES","MARCHANDISES"]', 5000.00, 124000.00,
    (now() - interval '4 months')::date, (now() + interval '8 months')::date,
    (SELECT json_agg(json_build_object('type', 'REMORQUE', 'id', id))::text
     FROM fleet.remorque WHERE carrosserie = 'FRIGORIFIQUE'),
    true, now(), 'system', now(), 'system', 0;

-- ---------------------------------------------------------------------------
-- Plans d'entretien, ordres de travail et sinistres
-- ---------------------------------------------------------------------------
DO $$
DECLARE
    v RECORD;
    r RECORD;
    i integer;
    k integer;
    garage uuid := 'dddddd00-0000-4000-8000-000000000001';
    ct uuid := 'dddddd00-0000-4000-8000-000000000005';
    pneus uuid := 'dddddd00-0000-4000-8000-000000000003';
    carrossier uuid := 'dddddd00-0000-4000-8000-000000000004';
    expert uuid := 'dddddd00-0000-4000-8000-000000000007';
    plan_id uuid;
    ot_id uuid;
    sin_id uuid;
    decalage integer;
    jour date;
    debut timestamp;
    mo numeric;
    piece numeric;
    ht numeric;
    lignes text;
    type_ot text;
    nature text;
    titre text;
    seq_ot integer := 0;
    seq_sin integer := 0;
    km_ot integer;
BEGIN
    -- Plans véhicules : révision 40 000 km / 12 mois et contrôle technique annuel.
    FOR v IN SELECT id, kilometrage, statut, (row_number() OVER (ORDER BY immatriculation))::int AS n
             FROM fleet.vehicule WHERE statut <> 'HORS_SERVICE' LOOP
        -- Kilomètres depuis la dernière révision : de 5 000 à 43 000 (quelques échus, d'autres en alerte).
        decalage := 5000 + ((v.n * 3700) % 38000) + CASE WHEN v.n % 7 = 0 THEN 3500 ELSE 0 END;
        plan_id := ('dddddd02-0000-4000-8000-' || lpad(v.n::text, 12, '0'))::uuid;
        INSERT INTO maintenance.plan_entretien (id, type_engin, engin_id, libelle, type_intervention,
            periodicite_km, periodicite_mois, periodicite_heures, seuil_alerte_km, seuil_alerte_jours,
            duree_estimee_min, cout_estime, prestataire_id, actif, derniere_date, derniere_km, derniere_heures,
            created_at, created_by, updated_at, updated_by, version)
        VALUES (plan_id, 'VEHICULE', v.id, 'Révision périodique (vidange, filtres, graissage)', 'ENTRETIEN_PREVENTIF',
            40000, 12, NULL, 2000, 15, 240, 6500.00, garage, true,
            (now() - make_interval(days => decalage / 320))::date, greatest(v.kilometrage - decalage, 0), NULL,
            now(), 'system', now(), 'system', 0);

        -- OT de la dernière révision, terminé, rattaché au plan.
        seq_ot := seq_ot + 1;
        jour := (now() - make_interval(days => decalage / 320))::date;
        mo := 3.5 * 680;
        piece := 1850 + (v.n % 5) * 220;
        ht := mo + piece;
        lignes := json_build_array(
            json_build_object('type', 'MAIN_OEUVRE', 'designation', 'Révision complète', 'referencePiece', NULL,
                'quantite', 3.5, 'prixUnitaireHt', json_build_object('montant', 680.00, 'devise', 'MAD'), 'tauxTva', 20),
            json_build_object('type', 'PIECE', 'designation', 'Kit filtres + huile moteur 40 L', 'referencePiece', 'KIT-REV-' || (v.n % 5),
                'quantite', 1, 'prixUnitaireHt', json_build_object('montant', piece, 'devise', 'MAD'), 'tauxTva', 20))::text;
        INSERT INTO maintenance.ordre_travail (id, reference, type_engin, engin_id, origine, plan_id, sinistre_id,
            type_intervention, nature, priorite, titre, description, prestataire_id, debut_planifie, fin_planifiee,
            immobilisation, budget_estime, statut, lignes_json, total_ht, total_ttc, debut_reel, fin_reelle,
            kilometrage, heures, diagnostic, travaux_realises, intervenant, numero_facture, date_facture,
            created_at, created_by, updated_at, updated_by, version)
        VALUES (gen_random_uuid(), 'OT-' || extract(year FROM jour)::int || '-' || lpad(seq_ot::text, 6, '0'),
            'VEHICULE', v.id, 'PLAN_ENTRETIEN', plan_id, NULL,
            'ENTRETIEN_PREVENTIF', 'PREVENTIF', 'NORMALE', 'Révision périodique', NULL, garage,
            jour + time '07:30', jour + time '11:30', true, 6500.00, 'TERMINE', lignes, ht, round(ht * 1.2, 2),
            jour + time '07:30', jour + time '11:15', greatest(v.kilometrage - decalage, 0), NULL,
            'RAS', 'Vidange moteur, remplacement des filtres, graissage', 'Garage PL Lyonnais',
            'FGPL-' || lpad(seq_ot::text, 5, '0'), jour, now(), 'system', now(), 'system', 0);

        -- Contrôle technique annuel (échéance calendaire).
        INSERT INTO maintenance.plan_entretien (id, type_engin, engin_id, libelle, type_intervention,
            periodicite_km, periodicite_mois, periodicite_heures, seuil_alerte_km, seuil_alerte_jours,
            duree_estimee_min, cout_estime, prestataire_id, actif, derniere_date, derniere_km, derniere_heures,
            created_at, created_by, updated_at, updated_by, version)
        VALUES (('dddddd03-0000-4000-8000-' || lpad(v.n::text, 12, '0'))::uuid, 'VEHICULE', v.id,
            'Contrôle technique annuel', 'CONTROLE_TECHNIQUE', NULL, 12, NULL, 0, 30, 120, 1800.00, ct, true,
            (now() - make_interval(days => 20 + (v.n * 37) % 360))::date, NULL, NULL,
            now(), 'system', now(), 'system', 0);
    END LOOP;

    -- Plans remorques : révision freins/essieux 60 000 km, groupe froid 1 500 h pour les frigorifiques.
    FOR r IN SELECT id, kilometrage, heures_groupe_froid, carrosserie, (row_number() OVER (ORDER BY immatriculation))::int AS n
             FROM fleet.remorque WHERE statut <> 'HORS_SERVICE' LOOP
        INSERT INTO maintenance.plan_entretien (id, type_engin, engin_id, libelle, type_intervention,
            periodicite_km, periodicite_mois, periodicite_heures, seuil_alerte_km, seuil_alerte_jours,
            duree_estimee_min, cout_estime, prestataire_id, actif, derniere_date, derniere_km, derniere_heures,
            created_at, created_by, updated_at, updated_by, version)
        VALUES (('dddddd04-0000-4000-8000-' || lpad(r.n::text, 12, '0'))::uuid, 'REMORQUE', r.id,
            'Révision freinage et essieux', 'FREINAGE', 60000, 12, NULL, 3000, 15, 180, 4200.00, garage, true,
            (now() - make_interval(days => 30 + (r.n * 53) % 330))::date,
            greatest(r.kilometrage - (8000 + (r.n * 6100) % 56000), 0), NULL,
            now(), 'system', now(), 'system', 0);
        IF r.carrosserie = 'FRIGORIFIQUE' THEN
            INSERT INTO maintenance.plan_entretien (id, type_engin, engin_id, libelle, type_intervention,
                periodicite_km, periodicite_mois, periodicite_heures, seuil_alerte_km, seuil_alerte_jours,
                duree_estimee_min, cout_estime, prestataire_id, actif, derniere_date, derniere_km, derniere_heures,
                created_at, created_by, updated_at, updated_by, version)
            VALUES (('dddddd05-0000-4000-8000-' || lpad(r.n::text, 12, '0'))::uuid, 'REMORQUE', r.id,
                'Entretien groupe froid', 'GROUPE_FROID', NULL, 6, 1500, 0, 20, 150, 3800.00, garage, true,
                (now() - interval '5 months')::date, NULL, greatest(r.heures_groupe_froid - 1300, 0),
                now(), 'system', now(), 'system', 0);
        END IF;
    END LOOP;

    -- Un an d'interventions correctives et pneumatiques (terminées).
    FOR i IN 1..28 LOOP
        SELECT id, kilometrage INTO v FROM fleet.vehicule
            WHERE statut <> 'HORS_SERVICE' ORDER BY immatriculation OFFSET (i * 7) % 25 LIMIT 1;
        seq_ot := seq_ot + 1;
        jour := (now() - make_interval(days => 12 * i + (i % 5)))::date;
        k := i % 4;
        type_ot := (ARRAY['REPARATION', 'PNEUMATIQUES', 'FREINAGE', 'DIAGNOSTIC'])[k + 1];
        nature := CASE WHEN k = 1 THEN 'PREVENTIF' ELSE 'CORRECTIF' END;
        titre := (ARRAY['Fuite circuit de refroidissement', 'Remplacement de 2 pneus directeurs',
                        'Plaquettes et disques de frein AV', 'Voyant moteur : diagnostic électronique'])[k + 1];
        mo := (1 + k) * 680;
        piece := (ARRAY[3200, 7800, 5400, 600])[k + 1] + (i % 3) * 250;
        ht := mo + piece;
        km_ot := greatest(v.kilometrage - i * 900, 0);
        lignes := json_build_array(
            json_build_object('type', 'MAIN_OEUVRE', 'designation', 'Main-d''œuvre atelier', 'referencePiece', NULL,
                'quantite', 1 + k, 'prixUnitaireHt', json_build_object('montant', 680.00, 'devise', 'MAD'), 'tauxTva', 20),
            json_build_object('type', CASE WHEN k = 3 THEN 'SOUS_TRAITANCE' ELSE 'PIECE' END,
                'designation', (ARRAY['Durite + liquide de refroidissement', 'Pneus 315/70 R22.5 (x2)', 'Kit freins AV', 'Valise diagnostic constructeur'])[k + 1],
                'referencePiece', (ARRAY['DUR-315', 'PN-31570', 'KFR-AV-22', NULL])[k + 1],
                'quantite', 1, 'prixUnitaireHt', json_build_object('montant', piece, 'devise', 'MAD'), 'tauxTva', 20))::text;
        INSERT INTO maintenance.ordre_travail (id, reference, type_engin, engin_id, origine, plan_id, sinistre_id,
            type_intervention, nature, priorite, titre, description, prestataire_id, debut_planifie, fin_planifiee,
            immobilisation, budget_estime, statut, lignes_json, total_ht, total_ttc, debut_reel, fin_reelle,
            kilometrage, heures, diagnostic, travaux_realises, intervenant, numero_facture, date_facture,
            created_at, created_by, updated_at, updated_by, version)
        VALUES (gen_random_uuid(), 'OT-' || extract(year FROM jour)::int || '-' || lpad(seq_ot::text, 6, '0'),
            'VEHICULE', v.id, CASE WHEN k = 3 THEN 'PANNE_SIGNALEE' ELSE 'MANUELLE' END, NULL, NULL,
            type_ot, nature, CASE WHEN k = 0 THEN 'HAUTE' ELSE 'NORMALE' END, titre,
            'Signalé par le chauffeur au retour de tournée.', CASE WHEN k = 1 THEN pneus ELSE garage END,
            jour + time '08:00', jour + time '16:00', true, round(ht * 0.95, 2), 'TERMINE', lignes, ht, round(ht * 1.2, 2),
            jour + time '08:10', jour + time '14:40', km_ot, NULL, titre, 'Intervention réalisée, essai routier OK.',
            'Atelier', 'F-' || lpad(seq_ot::text, 5, '0'), jour, now(), 'system', now(), 'system', 0);
    END LOOP;

    -- OT en cours pour les engins déjà EN_MAINTENANCE (cohérence avec le statut de flotte).
    FOR v IN SELECT id, 'VEHICULE' AS type_engin, kilometrage FROM fleet.vehicule WHERE statut = 'EN_MAINTENANCE'
             UNION ALL
             SELECT id, 'REMORQUE', kilometrage FROM fleet.remorque WHERE statut = 'EN_MAINTENANCE' LOOP
        seq_ot := seq_ot + 1;
        INSERT INTO maintenance.ordre_travail (id, reference, type_engin, engin_id, origine, plan_id, sinistre_id,
            type_intervention, nature, priorite, titre, description, prestataire_id, debut_planifie, fin_planifiee,
            immobilisation, budget_estime, statut, lignes_json, total_ht, total_ttc, debut_reel,
            created_at, created_by, updated_at, updated_by, version)
        VALUES (gen_random_uuid(), 'OT-' || extract(year FROM now())::int || '-' || lpad(seq_ot::text, 6, '0'),
            v.type_engin, v.id, 'MANUELLE', NULL, NULL,
            'REPARATION', 'CORRECTIF', 'HAUTE',
            CASE WHEN v.type_engin = 'VEHICULE' THEN 'Remplacement embrayage' ELSE 'Remplacement coussins de suspension' END,
            'Immobilisé à l''atelier.', garage,
            (now() - interval '1 day')::timestamp, (now() + interval '2 days')::timestamp, true, 14000.00,
            CASE WHEN seq_ot % 3 = 0 THEN 'EN_ATTENTE_PIECES' ELSE 'EN_COURS' END,
            json_build_array(json_build_object('type', 'MAIN_OEUVRE', 'designation', 'Dépose / repose', 'referencePiece', NULL,
                'quantite', 6, 'prixUnitaireHt', json_build_object('montant', 680.00, 'devise', 'MAD'), 'tauxTva', 20))::text,
            4080.00, 4896.00, (now() - interval '1 day')::timestamp, now(), 'system', now(), 'system', 0);
    END LOOP;

    -- OT planifiés à venir (révisions échues ou en alerte, pneus).
    FOR i IN 1..6 LOOP
        SELECT id INTO v FROM fleet.vehicule WHERE statut IN ('DISPONIBLE', 'RESERVE', 'EN_VOYAGE') ORDER BY immatriculation OFFSET i * 3 LIMIT 1;
        seq_ot := seq_ot + 1;
        INSERT INTO maintenance.ordre_travail (id, reference, type_engin, engin_id, origine, plan_id, sinistre_id,
            type_intervention, nature, priorite, titre, description, prestataire_id, debut_planifie, fin_planifiee,
            immobilisation, budget_estime, statut, lignes_json, total_ht, total_ttc,
            created_at, created_by, updated_at, updated_by, version)
        VALUES (gen_random_uuid(), 'OT-' || extract(year FROM now())::int || '-' || lpad(seq_ot::text, 6, '0'),
            'VEHICULE', v.id, 'MANUELLE', NULL, NULL,
            CASE WHEN i % 2 = 0 THEN 'PNEUMATIQUES' ELSE 'ENTRETIEN_PREVENTIF' END, 'PREVENTIF', 'NORMALE',
            CASE WHEN i % 2 = 0 THEN 'Permutation et contrôle pneumatiques' ELSE 'Révision à planifier' END,
            NULL, CASE WHEN i % 2 = 0 THEN pneus ELSE garage END,
            (date_trunc('day', now()) + make_interval(days => 3 + i * 4, hours => 7))::timestamp,
            (date_trunc('day', now()) + make_interval(days => 3 + i * 4, hours => 12))::timestamp,
            true, 4500.00, 'PLANIFIE', '[]', 0, 0, now(), 'system', now(), 'system', 0);
    END LOOP;

    -- Sinistres : ouverts sur les véhicules IMMOBILISE, puis historique varié.
    FOR v IN SELECT id, (row_number() OVER (ORDER BY immatriculation))::int AS n FROM fleet.vehicule WHERE statut = 'IMMOBILISE' LOOP
        seq_sin := seq_sin + 1;
        INSERT INTO maintenance.sinistre (id, reference, vehicule_id, remorque_id, chauffeur_id, voyage_id,
            date_survenance, lieu, latitude, longitude, type_sinistre, gravite, responsabilite, description,
            constat_amiable, rapport_police, blesses, engin_immobilise, tiers_nom, tiers_immatriculation,
            tiers_assureur, tiers_numero_police, contrat_id, numero_dossier_assureur, date_declaration_assureur,
            expert_id, date_expertise, estimation_dommages, franchise, indemnite, statut, date_cloture,
            created_at, created_by, updated_at, updated_by, version)
        VALUES (gen_random_uuid(), 'SIN-' || extract(year FROM now())::int || '-' || lpad(seq_sin::text, 6, '0'),
            v.id, NULL, ('55555550-0000-4000-8000-' || lpad(((v.n % 12) + 1)::text, 12, '0'))::uuid, NULL,
            (now() - make_interval(days => 3 + v.n * 2))::timestamp,
            (ARRAY['A7, sortie Vienne', 'Rocade Est, Lyon', 'Zone logistique de Corbas'])[(v.n % 3) + 1],
            45.60 + v.n * 0.05, 4.85, 'ACCIDENT_CIRCULATION', 'MATERIEL_LOURD', 'A_DETERMINER',
            'Collision arrière avec un véhicule léger en ralentissement ; face avant et radiateur endommagés.',
            true, v.n % 2 = 0, false, true, 'M. ' || (ARRAY['Durand', 'Lefèvre', 'Garnier'])[(v.n % 3) + 1],
            'AB-' || (100 + v.n) || '-CD', 'Assureur particulier', 'PART-' || v.n,
            'dddddd01-0000-4000-8000-000000000001'::uuid,
            CASE WHEN v.n % 2 = 0 THEN 'MT-SIN-' || lpad(seq_sin::text, 5, '0') END,
            CASE WHEN v.n % 2 = 0 THEN (now() - make_interval(days => 1 + v.n))::date END,
            CASE WHEN v.n % 2 = 0 THEN expert END, NULL,
            65000 + v.n * 8000, 10000.00, NULL,
            CASE WHEN v.n % 2 = 0 THEN 'EN_EXPERTISE' ELSE 'DECLARE' END, NULL,
            now(), 'system', now(), 'system', 0);
    END LOOP;

    FOR i IN 1..7 LOOP
        SELECT id INTO v FROM fleet.vehicule WHERE statut <> 'HORS_SERVICE' ORDER BY immatriculation OFFSET (i * 5) % 30 LIMIT 1;
        SELECT id INTO r FROM fleet.remorque ORDER BY immatriculation OFFSET i % 15 LIMIT 1;
        seq_sin := seq_sin + 1;
        sin_id := gen_random_uuid();
        jour := (now() - make_interval(days => 25 + i * 38))::date;
        INSERT INTO maintenance.sinistre (id, reference, vehicule_id, remorque_id, chauffeur_id, voyage_id,
            date_survenance, lieu, latitude, longitude, type_sinistre, gravite, responsabilite, description,
            constat_amiable, rapport_police, blesses, engin_immobilise, tiers_nom, tiers_immatriculation,
            tiers_assureur, tiers_numero_police, contrat_id, numero_dossier_assureur, date_declaration_assureur,
            expert_id, date_expertise, estimation_dommages, franchise, indemnite, statut, date_cloture,
            created_at, created_by, updated_at, updated_by, version)
        VALUES (sin_id, 'SIN-' || extract(year FROM jour)::int || '-' || lpad(seq_sin::text, 6, '0'),
            v.id, CASE WHEN i % 2 = 0 THEN r.id END,
            ('55555550-0000-4000-8000-' || lpad(((i % 12) + 1)::text, 12, '0'))::uuid, NULL,
            jour + time '15:20',
            (ARRAY['Quai 4, plateforme de Corbas', 'Parking poids lourds, Chalon-sur-Saône', 'A6, péage de Villefranche',
                   'Centre-ville de Grenoble', 'Aire de Saint-Rambert', 'Zone industrielle de Vénissieux', 'Port Édouard-Herriot'])[i],
            NULL, NULL,
            (ARRAY['ACCROCHAGE', 'BRIS_DE_GLACE', 'ACCIDENT_CIRCULATION', 'VANDALISME', 'ACCROCHAGE', 'DOMMAGE_MARCHANDISE', 'VOL'])[i],
            CASE WHEN i = 3 THEN 'MATERIEL_LOURD' ELSE 'MATERIEL_LEGER' END,
            (ARRAY['RESPONSABLE', 'NON_RESPONSABLE', 'PARTAGEE', 'NON_RESPONSABLE', 'RESPONSABLE', 'A_DETERMINER', 'NON_RESPONSABLE'])[i],
            (ARRAY['Accrochage en manœuvre à quai, porte arrière de la remorque enfoncée.',
                   'Impact de gravillon, pare-brise fissuré.',
                   'Refus de priorité d''un tiers au rond-point, aile avant droite endommagée.',
                   'Bâche de remorque lacérée pendant le stationnement de nuit.',
                   'Rétroviseur arraché contre un poteau en centre-ville.',
                   'Rupture de la chaîne du froid, marchandise refusée à la livraison.',
                   'Vol de gasoil par siphonnage du réservoir.'])[i],
            i IN (1, 3, 5), i IN (3, 4, 7), false, false,
            CASE WHEN i = 3 THEN 'Mme Rousseau' END, CASE WHEN i = 3 THEN 'EF-456-GH' END,
            CASE WHEN i = 3 THEN 'Assureur particulier' END, NULL,
            CASE WHEN i = 6 THEN 'dddddd01-0000-4000-8000-000000000002'::uuid ELSE 'dddddd01-0000-4000-8000-000000000001'::uuid END,
            'MT-SIN-' || lpad(seq_sin::text, 5, '0'), jour + 2,
            CASE WHEN i IN (3, 6) THEN expert END, CASE WHEN i IN (3, 6) THEN jour + 9 END,
            (ARRAY[18000, 6500, 74000, 9000, 3200, 42000, 3800])[i],
            CASE WHEN i = 6 THEN 5000.00 ELSE 10000.00 END,
            (ARRAY[8000, 6500, 64000, 9000, NULL, 37000, NULL])[i],
            CASE WHEN i = 5 THEN 'CLASSE_SANS_SUITE' WHEN i = 1 THEN 'EN_REPARATION' ELSE 'CLOS' END,
            CASE WHEN i IN (1) THEN NULL ELSE jour + 40 END,
            now(), 'system', now(), 'system', 0);

        -- Réparation liée (sauf vol et dommage marchandise).
        IF i NOT IN (6, 7) THEN
            seq_ot := seq_ot + 1;
            piece := (ARRAY[11000, 5200, 59000, 7000, 2500])[i];
            ht := piece + 4 * 680;
            INSERT INTO maintenance.ordre_travail (id, reference, type_engin, engin_id, origine, plan_id, sinistre_id,
                type_intervention, nature, priorite, titre, description, prestataire_id, debut_planifie, fin_planifiee,
                immobilisation, budget_estime, statut, lignes_json, total_ht, total_ttc, debut_reel, fin_reelle,
                kilometrage, heures, diagnostic, travaux_realises, intervenant, numero_facture, date_facture,
                created_at, created_by, updated_at, updated_by, version)
            VALUES (gen_random_uuid(), 'OT-' || extract(year FROM jour)::int || '-' || lpad(seq_ot::text, 6, '0'),
                CASE WHEN i IN (1, 4) AND r.id IS NOT NULL AND i % 2 = 0 THEN 'REMORQUE' ELSE 'VEHICULE' END,
                CASE WHEN i IN (1, 4) AND r.id IS NOT NULL AND i % 2 = 0 THEN r.id ELSE v.id END,
                'SINISTRE', NULL, sin_id,
                CASE WHEN i = 2 THEN 'AUTRE' ELSE 'CARROSSERIE' END, 'CORRECTIF', 'HAUTE',
                'Réparation suite sinistre', NULL, carrossier,
                (jour + 12) + time '08:00', (jour + 13) + time '17:00', true, round(ht, 2),
                CASE WHEN i = 1 THEN 'EN_COURS' ELSE 'TERMINE' END,
                json_build_array(
                    json_build_object('type', 'MAIN_OEUVRE', 'designation', 'Carrosserie / peinture', 'referencePiece', NULL,
                        'quantite', 4, 'prixUnitaireHt', json_build_object('montant', 680.00, 'devise', 'MAD'), 'tauxTva', 20),
                    json_build_object('type', 'PIECE', 'designation', 'Pièces de carrosserie', 'referencePiece', NULL,
                        'quantite', 1, 'prixUnitaireHt', json_build_object('montant', piece, 'devise', 'MAD'), 'tauxTva', 20))::text,
                ht, round(ht * 1.2, 2), (jour + 12) + time '08:00',
                CASE WHEN i = 1 THEN NULL ELSE (jour + 13) + time '16:00' END,
                NULL, NULL, NULL, CASE WHEN i = 1 THEN NULL ELSE 'Remise en état, contrôle qualité.' END,
                'Carrosserie Industrielle du Rhône', CASE WHEN i = 1 THEN NULL ELSE 'FCIR-' || lpad(seq_ot::text, 5, '0') END,
                CASE WHEN i = 1 THEN NULL ELSE jour + 14 END, now(), 'system', now(), 'system', 0);
        END IF;
    END LOOP;
END $$;
