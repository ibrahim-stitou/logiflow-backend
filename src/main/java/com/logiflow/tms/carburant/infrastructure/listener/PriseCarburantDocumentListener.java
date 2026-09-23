package com.logiflow.tms.carburant.infrastructure.listener;

import com.logiflow.tms.carburant.api.CarburantApi;
import com.logiflow.tms.document.api.DocumentEntiteModificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PriseCarburantDocumentListener {

  private final CarburantApi carburantApi;

  @ApplicationModuleListener
  void avantModificationDocument(DocumentEntiteModificationEvent event) {
    if ("PRISE_CARBURANT".equals(event.typeEntite())) {
      carburantApi.verifierPriseModifiable(event.entiteId());
    }
  }
}
