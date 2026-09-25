package com.logiflow.tms.planning.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logiflow.tms.dossier.api.DossierApi;
import com.logiflow.tms.planning.domain.model.ArretVoyage;
import com.logiflow.tms.planning.domain.port.out.VoyageArretRepository;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VoyageArretMaintenanceServiceTest {

  @Mock private VoyageArretRepository voyageArretRepository;
  @Mock private DossierApi dossierApi;

  @InjectMocks private VoyageArretMaintenanceService service;

  @Test
  void purgerArretsOrphelinsSupprimeLesArretsNonOriginauxSansReference() {
    UUID voyageId = UUID.randomUUID();
    UUID original = UUID.randomUUID();
    UUID insere = UUID.randomUUID();
    UUID dossierId = UUID.randomUUID();

    when(dossierApi.listerArretsVoyageReferences(List.of(dossierId))).thenReturn(Set.of(original));
    when(voyageArretRepository.parVoyageIdOrdonnes(voyageId))
        .thenReturn(List.of(arret(original, voyageId, 0, true), arret(insere, voyageId, 1, false)));

    int supprimes = service.purgerArretsOrphelins(voyageId, List.of(dossierId));

    assertThat(supprimes).isEqualTo(1);
    verify(voyageArretRepository).supprimerParId(insere);
    verify(voyageArretRepository).reindexerSequences(voyageId);
  }

  private static ArretVoyage arret(UUID id, UUID voyageId, int indice, boolean original) {
    return ArretVoyage.creer(id, voyageId, indice, "Stop", new GeoPoint(0, indice), null, original);
  }
}
