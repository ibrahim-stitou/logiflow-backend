package com.logiflow.tms.shared.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logiflow.tms.shared.domain.port.out.ReferenceSequenceStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReferenceGeneratorTest {

  @Mock private ReferenceSequenceStore sequenceStore;

  @InjectMocks private ReferenceGenerator referenceGenerator;

  @Test
  void reprendApresLaDerniereSequencePersistee() {
    when(sequenceStore.derniereSequence("DT", 2026)).thenReturn(3L);

    assertThat(referenceGenerator.generer("DT", 2026).valeur()).isEqualTo("DT-2026-000004");
    assertThat(referenceGenerator.generer("DT", 2026).valeur()).isEqualTo("DT-2026-000005");
    verify(sequenceStore).derniereSequence("DT", 2026);
  }

  @Test
  void isoleLesCompteursParPrefixeEtAnnee() {
    when(sequenceStore.derniereSequence("DT", 2026)).thenReturn(0L);
    when(sequenceStore.derniereSequence("CMD", 2026)).thenReturn(10L);

    assertThat(referenceGenerator.generer("DT", 2026).valeur()).isEqualTo("DT-2026-000001");
    assertThat(referenceGenerator.generer("CMD", 2026).valeur()).isEqualTo("CMD-2026-000011");
  }
}
