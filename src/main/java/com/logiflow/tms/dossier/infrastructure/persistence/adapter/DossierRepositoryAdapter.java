package com.logiflow.tms.dossier.infrastructure.persistence.adapter;

import com.logiflow.tms.dossier.domain.model.DossierTransport;
import com.logiflow.tms.dossier.domain.port.out.DossierTransportRepository;
import com.logiflow.tms.dossier.infrastructure.persistence.entity.DossierEntity;
import com.logiflow.tms.dossier.infrastructure.persistence.mapper.DossierMapper;
import com.logiflow.tms.dossier.infrastructure.persistence.repository.DossierJpaRepository;
import com.logiflow.tms.dossier.infrastructure.persistence.repository.LigneMarchandiseJpaRepository;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DossierRepositoryAdapter implements DossierTransportRepository {

  private final DossierJpaRepository jpaRepository;
  private final LigneMarchandiseJpaRepository ligneJpaRepository;
  private final DossierMapper mapper;

  @Override
  public DossierTransport sauvegarder(DossierTransport dossier) {
    DossierEntity entite =
        jpaRepository
            .findById(dossier.id())
            .map(
                existante -> {
                  mapper.mettreAJour(existante, dossier);
                  return existante;
                })
            .orElseGet(() -> mapper.versEntite(dossier));
    DossierEntity entiteSauvegardee = jpaRepository.save(entite);
    ligneJpaRepository.deleteByDossierId(entiteSauvegardee.getId());
    var lignesEntites =
        dossier.lignesMarchandise().stream()
            .map(ligne -> mapper.versLigneEntite(entiteSauvegardee.getId(), ligne))
            .toList();
    ligneJpaRepository.saveAll(lignesEntites);
    return mapper.versDomaine(
        entiteSauvegardee, ligneJpaRepository.findByDossierId(entiteSauvegardee.getId()));
  }

  @Override
  public Optional<DossierTransport> parId(UUID id) {
    return jpaRepository
        .findById(id)
        .map(entite -> mapper.versDomaine(entite, ligneJpaRepository.findByDossierId(id)));
  }

  @Override
  public Optional<DossierTransport> parReference(String reference) {
    return jpaRepository
        .findByReference(reference)
        .map(
            entite ->
                mapper.versDomaine(entite, ligneJpaRepository.findByDossierId(entite.getId())));
  }

  @Override
  public List<DossierTransport> parCommandeId(UUID commandeId) {
    return jpaRepository.findByCommandeId(commandeId).stream()
        .map(
            (DossierEntity entite) ->
                mapper.versDomaine(entite, ligneJpaRepository.findByDossierId(entite.getId())))
        .toList();
  }

  @Override
  public Page<DossierTransport> rechercher(String texteRecherche, PageRequest pageRequest) {
    var pageable =
        org.springframework.data.domain.PageRequest.of(pageRequest.numero(), pageRequest.taille());
    org.springframework.data.domain.Page<DossierEntity> pageJpa =
        (texteRecherche == null || texteRecherche.isBlank())
            ? jpaRepository.findAll(pageable)
            : jpaRepository.findByReferenceContainingIgnoreCase(texteRecherche, pageable);
    var contenu =
        pageJpa.getContent().stream()
            .map(
                (DossierEntity entite) ->
                    mapper.versDomaine(entite, ligneJpaRepository.findByDossierId(entite.getId())))
            .toList();
    return Page.of(contenu, pageJpa.getNumber(), pageJpa.getSize(), pageJpa.getTotalElements());
  }
}
