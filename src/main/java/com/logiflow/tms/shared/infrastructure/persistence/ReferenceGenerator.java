package com.logiflow.tms.shared.infrastructure.persistence;

import com.logiflow.tms.shared.domain.port.out.ReferenceSequenceStore;
import com.logiflow.tms.shared.domain.vo.Reference;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Génère des {@link Reference} séquentielles par préfixe et par année.
 *
 * <p>Le compteur en mémoire est initialisé depuis la base à la première utilisation d'une clé
 * préfixe/année, ce qui évite les collisions après redémarrage. À remplacer par une séquence
 * PostgreSQL partagée dès qu'un déploiement multi-instance est envisagé.
 */
@Component
@RequiredArgsConstructor
public class ReferenceGenerator {

  private final ReferenceSequenceStore sequenceStore;
  private final Map<String, AtomicLong> compteurs = new ConcurrentHashMap<>();

  public Reference generer(String prefixe, int annee) {
    String cle = prefixe.toUpperCase(Locale.ROOT) + "-" + annee;
    AtomicLong compteur =
        compteurs.computeIfAbsent(
            cle, ignore -> new AtomicLong(sequenceStore.derniereSequence(prefixe, annee)));
    long sequence = compteur.incrementAndGet();
    return Reference.generer(prefixe, annee, sequence);
  }
}
