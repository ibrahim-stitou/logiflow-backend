-- Corrige les véhicules et remorques issus de V21 : groupe_froid=true exige temperature_min/max.

UPDATE fleet.vehicule
SET temperature_min = -25.0,
    temperature_max = 5.0,
    updated_at = now(),
    updated_by = 'system'
WHERE groupe_froid = true
  AND (temperature_min IS NULL OR temperature_max IS NULL);

UPDATE fleet.remorque
SET temperature_min = -25.0,
    temperature_max = 5.0,
    updated_at = now(),
    updated_by = 'system'
WHERE groupe_froid = true
  AND (temperature_min IS NULL OR temperature_max IS NULL);
