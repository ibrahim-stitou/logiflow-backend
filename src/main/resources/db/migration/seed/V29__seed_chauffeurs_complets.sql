-- Données de démonstration : profils complets des 12 chauffeurs (V21), pour la gestion des
-- chauffeurs et la planification (permis, habilitations, site de rattachement, passeport).
--
-- Répartition voulue :
--   * 9 chauffeurs affectables (ACTIF + DISPONIBLE) ; DRV-0002 EN_VOYAGE, DRV-0003 EN_REPOS,
--     DRV-0011 EN_CONGE, DRV-0012 SUSPENDU ;
--   * ADR : DRV-0001, 0004, 0007, 0009 ;
--   * permis CE (semi-remorque) pour tous sauf DRV-0006 et DRV-0010 (C seul : porteurs) ;
--   * passeport valide (voyages internationaux) : DRV-0001, 0005, 0008 ;
--   * visite médicale expirant sous 20 jours : DRV-0004 (alerte « bientôt expiré ») ;
--   * soldes de conduite réalistes (35 h à 56 h hebdomadaires).
-- Les dates sont relatives à la date d'application pour rester cohérentes dans le temps.

UPDATE driver.chauffeur c
SET
    cin = 'CIN' || lpad(n.i::text, 6, '0'),
    date_naissance = DATE '1975-03-01' + (n.i * 811) % 7300,
    lieu_naissance = (ARRAY['Lille', 'Lyon', 'Marseille', 'Nantes', 'Rouen', 'Toulouse',
                            'Metz', 'Dijon', 'Bordeaux', 'Reims', 'Nice', 'Tours'])[n.i],
    nationalite = CASE WHEN n.i = 8 THEN 'Marocaine' ELSE 'Française' END,
    adresse = n.i || ' rue des Transporteurs, Zone logistique',
    numero_permis = 'PERM-' || lpad(n.i::text, 8, '0'),
    categorie_permis = CASE WHEN n.i IN (6, 10) THEN 'B,C' ELSE 'B,C,CE' END,
    date_obtention_permis = DATE '2005-06-01' + (n.i * 97),
    date_expiration_permis = CURRENT_DATE + (400 + n.i * 90),
    numero_passeport = CASE WHEN n.i IN (1, 5, 8) THEN 'PA' || lpad((n.i * 7919)::text, 7, '0') END,
    date_delivrance_passeport = CASE WHEN n.i IN (1, 5, 8) THEN CURRENT_DATE - 900 END,
    date_expiration_passeport = CASE WHEN n.i IN (1, 5, 8) THEN CURRENT_DATE + 2700 END,
    pays_delivrance_passeport = CASE WHEN n.i IN (1, 5) THEN 'France' WHEN n.i = 8 THEN 'Maroc' END,
    date_embauche = DATE '2012-01-15' + (n.i * 233),
    type_contrat = (ARRAY['CDI', 'CDI', 'CDI', 'CDD', 'CDI', 'INTERIM'])[1 + (n.i - 1) % 6],
    experience_annees = 3 + (n.i * 7) % 18,
    specialisation = CASE
        WHEN n.i IN (1, 4, 7, 9) THEN 'Matières dangereuses (ADR)'
        WHEN n.i IN (3, 6) THEN 'Frigorifique'
        ELSE 'Messagerie / lots complets' END,
    -- Bases réparties sur les sites de démonstration (cf. V21 : villes 1 à 12).
    site_rattachement_id = ('33333330-0000-4000-8000-' || lpad(n.i::text, 12, '0'))::uuid,
    statut = CASE WHEN n.i = 12 THEN 'SUSPENDU' ELSE 'ACTIF' END,
    disponibilite = CASE n.i
        WHEN 2 THEN 'EN_VOYAGE'
        WHEN 3 THEN 'EN_REPOS'
        WHEN 11 THEN 'EN_CONGE'
        ELSE 'DISPONIBLE' END,
    solde_temps_conduite_minutes = 2100 + (n.i * 157) % 1260,
    habilitations_json = (
        '[{"type":"FIMO_FCO","reference":"FCO-' || lpad(n.i::text, 5, '0') || '",'
            || '"dateObtention":"' || (CURRENT_DATE - 700) || '",'
            || '"dateExpiration":"' || (CURRENT_DATE + 1100) || '"},'
        || '{"type":"CARTE_CONDUCTEUR","reference":"CC-' || lpad(n.i::text, 5, '0') || '",'
            || '"dateObtention":"' || (CURRENT_DATE - 500) || '",'
            || '"dateExpiration":"' || (CURRENT_DATE + 1300) || '"},'
        || '{"type":"VISITE_MEDICALE","reference":"VM-' || lpad(n.i::text, 5, '0') || '",'
            || '"dateObtention":"' || (CURRENT_DATE - 300) || '",'
            || '"dateExpiration":"'
            || (CASE WHEN n.i = 4 THEN CURRENT_DATE + 18 ELSE CURRENT_DATE + 400 END) || '"}'
        || CASE WHEN n.i IN (1, 4, 7, 9) THEN
            ',{"type":"ADR_BASE","reference":"ADR-' || lpad(n.i::text, 5, '0') || '",'
            || '"dateObtention":"' || (CURRENT_DATE - 200) || '",'
            || '"dateExpiration":"' || (CURRENT_DATE + 1600) || '"}'
           ELSE '' END
        || ']'),
    updated_at = now(),
    updated_by = 'system'
FROM (
    SELECT i, ('55555550-0000-4000-8000-' || lpad(i::text, 12, '0'))::uuid AS id
    FROM generate_series(1, 12) AS i
) AS n
WHERE c.id = n.id;
