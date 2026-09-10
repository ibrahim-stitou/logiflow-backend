package com.logiflow.tms.shared.infrastructure.persistence;

import com.logiflow.tms.shared.domain.port.out.ReferenceSequenceStore;
import com.logiflow.tms.shared.domain.vo.Reference;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Repository;

/** Lit la dernière séquence persistée dans les tables portant une référence métier. */
@Repository
public class JpaReferenceSequenceStore implements ReferenceSequenceStore {

  private static final String REFERENCES_PAR_MOTIF =
      """
      SELECT reference FROM dossier.dossier_transport WHERE reference LIKE :pattern
      UNION ALL
      SELECT reference FROM commande.commande WHERE reference LIKE :pattern
      UNION ALL
      SELECT reference FROM planning.voyage WHERE reference LIKE :pattern
      """;

  @PersistenceContext private EntityManager entityManager;

  @Override
  public long derniereSequence(String prefixe, int annee) {
    String motif =
        prefixe.toUpperCase(Locale.ROOT) + "-" + annee + "-%";
    @SuppressWarnings("unchecked")
    List<String> references =
        entityManager
            .createNativeQuery(REFERENCES_PAR_MOTIF)
            .setParameter("pattern", motif)
            .getResultList();

    return references.stream().mapToLong(Reference::extraireSequence).max().orElse(0L);
  }
}
