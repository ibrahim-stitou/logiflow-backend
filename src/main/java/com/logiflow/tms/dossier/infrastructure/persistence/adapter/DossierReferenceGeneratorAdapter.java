package com.logiflow.tms.dossier.infrastructure.persistence.adapter;

import com.logiflow.tms.dossier.domain.port.out.SequenceReferenceGenerator;
import com.logiflow.tms.shared.domain.vo.Reference;
import com.logiflow.tms.shared.infrastructure.persistence.ReferenceGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Délègue au générateur technique partagé ({@code shared.infrastructure.persistence}). */
@Component("dossierReferenceGeneratorAdapter")
@RequiredArgsConstructor
public class DossierReferenceGeneratorAdapter implements SequenceReferenceGenerator {

  private final ReferenceGenerator referenceGenerator;

  @Override
  public Reference generer(String prefixe, int annee) {
    return referenceGenerator.generer(prefixe, annee);
  }
}
