package com.logiflow.tms.shared.infrastructure.persistence;

import com.logiflow.tms.shared.domain.vo.Reference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/**
 * Génère des {@link Reference} séquentielles par préfixe et par année.
 *
 * <p>Implémentation en mémoire, suffisante pour une instance unique de l'application. À remplacer
 * par une séquence PostgreSQL partagée dès qu'un déploiement multi-instance est envisagé.
 */
@Component
public class ReferenceGenerator {

  private final Map<String, AtomicLong> compteurs = new ConcurrentHashMap<>();

  public Reference generer(String prefixe, int annee) {
    String cle = prefixe + "-" + annee;
    long sequence = compteurs.computeIfAbsent(cle, ignore -> new AtomicLong(0)).incrementAndGet();
    return Reference.generer(prefixe, annee, sequence);
  }
}
