-- Arrêts d'itinéraire et liaisons dossier ↔ arrêts pour les voyages démo avec remorque.
-- Dérivé des segments des dossiers rattachés (ordre de visite = chargement puis déchargement par dossier).

DO $$
DECLARE
    v record;
    d_id text;
    site_ch uuid;
    site_de uuid;
    ordered_sites uuid[] := '{}';
    site uuid;
    arret_uuid uuid;
    seq int;
    voyage_num integer;
    ch_arret uuid;
    de_arret uuid;
    ch_idx int;
    de_idx int;
    site_libelle text;
    site_lat double precision;
    site_lng double precision;
BEGIN
    FOR v IN
        SELECT
            voy.id AS voyage_id,
            voy.dossier_ids_json::jsonb AS dossiers,
            substring(voy.reference FROM 'VOY-2026-([0-9]+)')::integer AS voyage_num
        FROM planning.voyage voy
        WHERE voy.remorque_id IS NOT NULL
          AND jsonb_array_length(voy.dossier_ids_json::jsonb) > 0
          AND NOT EXISTS (
              SELECT 1 FROM planning.voyage_arret va WHERE va.voyage_id = voy.id
          )
    LOOP
        ordered_sites := '{}';

        FOR d_id IN SELECT jsonb_array_elements_text(v.dossiers)
        LOOP
            SELECT
                (dt.segments_json::json->0->>'siteId')::uuid,
                (dt.segments_json::json->1->>'siteId')::uuid
            INTO site_ch, site_de
            FROM dossier.dossier_transport dt
            WHERE dt.id = d_id::uuid;

            IF site_ch IS NOT NULL AND NOT site_ch = ANY (ordered_sites) THEN
                ordered_sites := array_append(ordered_sites, site_ch);
            END IF;
            IF site_de IS NOT NULL AND NOT site_de = ANY (ordered_sites) THEN
                ordered_sites := array_append(ordered_sites, site_de);
            END IF;
        END LOOP;

        IF coalesce(array_length(ordered_sites, 1), 0) < 2 THEN
            CONTINUE;
        END IF;

        voyage_num := v.voyage_num;
        seq := 0;
        FOREACH site IN ARRAY ordered_sites
        LOOP
            arret_uuid := (
                'ddddddd0-0000-4000-8000-'
                || lpad(voyage_num::text, 6, '0')
                || lpad(seq::text, 6, '0')
            )::uuid;

            SELECT s.libelle,
                   ST_Y(s.localisation::geometry),
                   ST_X(s.localisation::geometry)
            INTO site_libelle, site_lat, site_lng
            FROM referential.site s
            WHERE s.id = site;

            INSERT INTO planning.voyage_arret (
                id, voyage_id, indice_sequence, libelle, latitude, longitude, site_id,
                est_original, created_at, created_by, updated_at, updated_by, version
            ) VALUES (
                arret_uuid,
                v.voyage_id,
                seq,
                coalesce(site_libelle, 'Arrêt ' || seq),
                coalesce(site_lat, 0),
                coalesce(site_lng, 0),
                site,
                true,
                now(), 'system', now(), 'system', 0
            );

            seq := seq + 1;
        END LOOP;

        FOR d_id IN SELECT jsonb_array_elements_text(v.dossiers)
        LOOP
            SELECT
                (dt.segments_json::json->0->>'siteId')::uuid,
                (dt.segments_json::json->1->>'siteId')::uuid
            INTO site_ch, site_de
            FROM dossier.dossier_transport dt
            WHERE dt.id = d_id::uuid;

            IF site_ch IS NULL OR site_de IS NULL THEN
                CONTINUE;
            END IF;

            ch_arret := (
                'ddddddd0-0000-4000-8000-'
                || lpad(voyage_num::text, 6, '0')
                || lpad(
                    (
                        SELECT va.indice_sequence
                        FROM planning.voyage_arret va
                        WHERE va.voyage_id = v.voyage_id AND va.site_id = site_ch
                    )::text,
                    6,
                    '0'
                )
            )::uuid;

            de_arret := (
                'ddddddd0-0000-4000-8000-'
                || lpad(voyage_num::text, 6, '0')
                || lpad(
                    (
                        SELECT va.indice_sequence
                        FROM planning.voyage_arret va
                        WHERE va.voyage_id = v.voyage_id AND va.site_id = site_de
                    )::text,
                    6,
                    '0'
                )
            )::uuid;

            UPDATE dossier.dossier_transport
            SET arret_chargement_id = ch_arret,
                arret_dechargement_id = de_arret
            WHERE id = d_id::uuid;
        END LOOP;
    END LOOP;
END $$;
