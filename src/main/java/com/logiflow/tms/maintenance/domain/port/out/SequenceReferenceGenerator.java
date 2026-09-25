package com.logiflow.tms.maintenance.domain.port.out;

import com.logiflow.tms.shared.domain.vo.Reference;

/** Génère les références métier séquentielles (OT-2026-000001, SIN-2026-000001). */
public interface SequenceReferenceGenerator {

  Reference generer(String prefixe, int annee);
}
