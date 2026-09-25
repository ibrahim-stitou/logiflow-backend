package com.logiflow.tms.maintenance.application;

import com.logiflow.tms.maintenance.domain.model.Prestataire;
import com.logiflow.tms.maintenance.domain.model.TypePrestataire;
import com.logiflow.tms.maintenance.domain.port.out.PrestataireRepository;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gestion des prestataires (garages, assureurs, experts…). */
@Service
@RequiredArgsConstructor
public class PrestataireService {

  private final PrestataireRepository repository;

  @Transactional
  public Prestataire creer(String code, Prestataire.Fiche fiche) {
    if (code != null && repository.existeParCode(code.strip().toUpperCase(Locale.ROOT))) {
      throw new BusinessException("Un prestataire porte déjà le code " + code);
    }
    return repository.sauvegarder(Prestataire.creer(UUID.randomUUID(), code, fiche));
  }

  @Transactional
  public Prestataire modifier(UUID id, Prestataire.Fiche fiche) {
    Prestataire prestataire = consulter(id);
    prestataire.modifier(fiche);
    return repository.sauvegarder(prestataire);
  }

  @Transactional(readOnly = true)
  public Prestataire consulter(UUID id) {
    return repository
        .parId(id)
        .orElseThrow(
            () -> new NotFoundException("Aucun prestataire trouvé pour l'identifiant " + id));
  }

  @Transactional(readOnly = true)
  public Page<Prestataire> rechercher(
      String texte, TypePrestataire type, Boolean actif, PageRequest pageRequest) {
    return repository.rechercher(texte, type, actif, pageRequest);
  }

  /** Vérifie l'existence et, si demandé, le métier d'un prestataire référencé. */
  @Transactional(readOnly = true)
  public void verifier(UUID id, TypePrestataire typeAttendu) {
    if (id == null) {
      return;
    }
    Prestataire prestataire = consulter(id);
    if (typeAttendu != null && prestataire.fiche().type() != typeAttendu) {
      throw new BusinessException(
          prestataire.fiche().raisonSociale() + " n'est pas un prestataire de type " + typeAttendu);
    }
  }
}
