package com.logiflow.tms.referential.application;

import com.logiflow.tms.referential.api.MarchandiseApi;
import com.logiflow.tms.referential.api.dto.MarchandiseSummary;
import com.logiflow.tms.referential.domain.model.Marchandise;
import com.logiflow.tms.referential.domain.port.out.MarchandiseRepository;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.ConflictException;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cas d'utilisation applicatifs du sous-domaine Marchandise. */
@Service
@RequiredArgsConstructor
public class MarchandiseService implements MarchandiseApi {

  private final MarchandiseRepository marchandiseRepository;

  @Transactional
  public UUID creerMarchandise(
      String code,
      String libelle,
      String famille,
      String classeAdr,
      String numeroOnu,
      boolean gerbable) {
    if (marchandiseRepository.existeParCode(code)) {
      throw new ConflictException("Une marchandise avec le code '%s' existe déjà".formatted(code));
    }
    Marchandise marchandise =
        Marchandise.creer(
            UUID.randomUUID(), code, libelle, famille, classeAdr, numeroOnu, gerbable);
    return marchandiseRepository.sauvegarder(marchandise).id();
  }

  @Transactional
  public void renommerMarchandise(UUID id, String libelle) {
    Marchandise marchandise = trouverOuEchouer(id);
    marchandise.renommer(libelle);
    marchandiseRepository.sauvegarder(marchandise);
  }

  @Transactional
  public void desactiverMarchandise(UUID id) {
    Marchandise marchandise = trouverOuEchouer(id);
    marchandise.desactiver();
    marchandiseRepository.sauvegarder(marchandise);
  }

  @Transactional(readOnly = true)
  public Marchandise consulterMarchandise(UUID id) {
    return trouverOuEchouer(id);
  }

  @Transactional(readOnly = true)
  public Page<Marchandise> listerMarchandises(String texteRecherche, PageRequest pageRequest) {
    return marchandiseRepository.rechercher(texteRecherche, pageRequest);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<MarchandiseSummary> consulter(UUID marchandiseId) {
    return marchandiseRepository.parId(marchandiseId).map(this::versResume);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean estActif(UUID marchandiseId) {
    return marchandiseRepository.parId(marchandiseId).map(Marchandise::estActif).orElse(false);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean estDangereuse(UUID marchandiseId) {
    return marchandiseRepository.parId(marchandiseId).map(Marchandise::estDangereuse).orElse(false);
  }

  private MarchandiseSummary versResume(Marchandise marchandise) {
    return new MarchandiseSummary(
        marchandise.id(),
        marchandise.code(),
        marchandise.libelle(),
        marchandise.famille(),
        marchandise.classeAdr(),
        marchandise.numeroOnu(),
        marchandise.gerbable(),
        marchandise.estActif());
  }

  private Marchandise trouverOuEchouer(UUID id) {
    return marchandiseRepository
        .parId(id)
        .orElseThrow(
            () -> new NotFoundException("Aucune marchandise trouvée pour l'identifiant " + id));
  }
}
