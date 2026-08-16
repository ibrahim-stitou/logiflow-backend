package com.logiflow.tms.ai.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.logiflow.tms.ai.domain.model.CandidatDossier;
import com.logiflow.tms.ai.domain.model.PropositionGroupage;
import com.logiflow.tms.ai.domain.service.AiDomainService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AiDomainServiceTest {

  private final AiDomainService service = new AiDomainService();

  @Test
  void repliSansIaRegroupeLesDossiersGroupables() {
    CandidatDossier d1 =
        new CandidatDossier(UUID.randomUUID(), "DT-2026-000001", 500, 2.5, 10, false, true);
    CandidatDossier d2 =
        new CandidatDossier(UUID.randomUUID(), "DT-2026-000002", 300, 1.5, 5, false, true);

    List<PropositionGroupage> propositions = service.repliSansIa(List.of(d1, d2));

    assertThat(propositions).hasSize(1);
    assertThat(propositions.get(0).genereParIa()).isFalse();
    assertThat(propositions.get(0).dossierIds()).containsExactlyInAnyOrder(d1.id(), d2.id());
  }

  @Test
  void repliSansIaIgnoreLesDossiersNonGroupables() {
    CandidatDossier groupable =
        new CandidatDossier(UUID.randomUUID(), "DT-2026-000001", 500, 2.5, 10, false, true);
    CandidatDossier nonGroupable =
        new CandidatDossier(UUID.randomUUID(), "DT-2026-000002", 300, 1.5, 5, false, false);

    List<PropositionGroupage> propositions = service.repliSansIa(List.of(groupable, nonGroupable));

    assertThat(propositions).isEmpty();
  }

  @Test
  void uneAyantMoinsDeDeuxDossiersEstRefusee() {
    UUID unique = UUID.randomUUID();
    assertThatThrownBy(
            () ->
                new PropositionGroupage(
                    List.of(unique), null, null, null, null, "justification", false))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
