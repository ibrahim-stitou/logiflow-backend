package com.logiflow.tms.order.infrastructure.persistence.adapter;

import com.logiflow.tms.order.domain.model.Commande;
import com.logiflow.tms.order.domain.port.out.CommandeRepository;
import com.logiflow.tms.order.infrastructure.persistence.mapper.CommandeMapper;
import com.logiflow.tms.order.infrastructure.persistence.repository.CommandeJpaRepository;
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
  private final CommandeMapper mapper;

  @Override
  public Commande sauvegarder(Commande commande) {
    var entite = jpaRepository.save(mapper.versEntite(commande));
    return mapper.versDomaine(entite);
  }

  @Override
  public Optional<Commande> parId(UUID id) {
    return jpaRepository.findById(id).map(mapper::versDomaine);
  }

  @Override
  public Optional<Commande> parReference(String reference) {
    return jpaRepository.findByReference(reference).map(mapper::versDomaine);
  }

  @Override
  public Page<Commande> rechercher(PageRequest pageRequest) {
    var pageable =
        org.springframework.data.domain.PageRequest.of(pageRequest.numero(), pageRequest.taille());
    var pageJpa = jpaRepository.findAll(pageable);
    var contenu = pageJpa.getContent().stream().map(mapper::versDomaine).toList();
    return Page.of(contenu, pageJpa.getNumber(), pageJpa.getSize(), pageJpa.getTotalElements());
  }
}
