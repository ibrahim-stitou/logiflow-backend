package com.logiflow.tms.order.infrastructure.persistence.adapter;

import com.logiflow.tms.order.domain.model.Commande;
import com.logiflow.tms.order.domain.port.out.CommandeRepository;
import com.logiflow.tms.order.infrastructure.persistence.entity.CommandeEntity;
import com.logiflow.tms.order.infrastructure.persistence.mapper.CommandeMapper;
import com.logiflow.tms.order.infrastructure.persistence.repository.CommandeJpaRepository;
import com.logiflow.tms.order.infrastructure.persistence.repository.LigneCommandeJpaRepository;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommandeRepositoryAdapter implements CommandeRepository {

  private final CommandeJpaRepository jpaRepository;
  private final LigneCommandeJpaRepository ligneJpaRepository;
  private final CommandeMapper mapper;

  @Override
  public Commande sauvegarder(Commande commande) {
    var entite = jpaRepository.save(mapper.versEntite(commande));
    ligneJpaRepository.deleteByCommandeId(entite.getId());
    var lignesEntites =
        commande.lignes().stream().map(ligne -> mapper.versLigneEntite(entite.getId(), ligne)).toList();
    ligneJpaRepository.saveAll(lignesEntites);
    return mapper.versDomaine(entite, ligneJpaRepository.findByCommandeId(entite.getId()));
  }

  @Override
  public Optional<Commande> parId(UUID id) {
    return jpaRepository
        .findById(id)
        .map(entite -> mapper.versDomaine(entite, ligneJpaRepository.findByCommandeId(id)));
  }

  @Override
  public Optional<Commande> parReference(String reference) {
    return jpaRepository
        .findByReference(reference)
        .map(entite -> mapper.versDomaine(entite, ligneJpaRepository.findByCommandeId(entite.getId())));
  }

  @Override
  public Page<Commande> rechercher(String texteRecherche, PageRequest pageRequest) {
    var pageable =
        org.springframework.data.domain.PageRequest.of(pageRequest.numero(), pageRequest.taille());
    org.springframework.data.domain.Page<CommandeEntity> pageJpa =
        (texteRecherche == null || texteRecherche.isBlank())
            ? jpaRepository.findAll(pageable)
            : jpaRepository.findByReferenceContainingIgnoreCase(texteRecherche, pageable);
    var contenu =
        pageJpa.getContent().stream()
            .map(
                (CommandeEntity entite) ->
                    mapper.versDomaine(entite, ligneJpaRepository.findByCommandeId(entite.getId())))
            .toList();
    return Page.of(contenu, pageJpa.getNumber(), pageJpa.getSize(), pageJpa.getTotalElements());
  }
}
