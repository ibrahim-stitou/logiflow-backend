package com.logiflow.tms.dossier.infrastructure.persistence.adapter;

import com.logiflow.tms.dossier.domain.model.DossierTransport;
import com.logiflow.tms.dossier.domain.port.out.DossierTransportRepository;
import com.logiflow.tms.dossier.infrastructure.persistence.mapper.DossierMapper;
import com.logiflow.tms.dossier.infrastructure.persistence.repository.DossierJpaRepository;
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
  private final DossierMapper mapper;

  @Override
  public DossierTransport sauvegarder(DossierTransport dossier) {
    var entite = jpaRepository.save(mapper.versEntite(dossier));
    return mapper.versDomaine(entite);
  }

  @Override
  public Optional<DossierTransport> parId(UUID id) {
    return jpaRepository.findById(id).map(mapper::versDomaine);
  }

  @Override
  public Optional<DossierTransport> parReference(String reference) {
    return jpaRepository.findByReference(reference).map(mapper::versDomaine);
  }

  @Override
  public List<DossierTransport> parCommandeId(UUID commandeId) {
    return jpaRepository.findByCommandeId(commandeId).stream().map(mapper::versDomaine).toList();
  }

  @Override
  public Page<DossierTransport> rechercher(PageRequest pageRequest) {
    var pageable =
        org.springframework.data.domain.PageRequest.of(pageRequest.numero(), pageRequest.taille());
    var pageJpa = jpaRepository.findAll(pageable);
    var contenu = pageJpa.getContent().stream().map(mapper::versDomaine).toList();
    return Page.of(contenu, pageJpa.getNumber(), pageJpa.getSize(), pageJpa.getTotalElements());
  }
}
