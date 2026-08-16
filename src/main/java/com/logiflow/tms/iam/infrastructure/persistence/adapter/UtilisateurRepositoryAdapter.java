package com.logiflow.tms.iam.infrastructure.persistence.adapter;

import com.logiflow.tms.iam.domain.model.Utilisateur;
import com.logiflow.tms.iam.domain.port.out.UtilisateurRepository;
import com.logiflow.tms.iam.infrastructure.persistence.entity.UtilisateurEntity;
import com.logiflow.tms.iam.infrastructure.persistence.mapper.UtilisateurMapper;
import com.logiflow.tms.iam.infrastructure.persistence.repository.UtilisateurJpaRepository;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UtilisateurRepositoryAdapter implements UtilisateurRepository {

  private final UtilisateurJpaRepository jpaRepository;
  private final UtilisateurMapper mapper;

  @Override
  public Utilisateur sauvegarder(Utilisateur utilisateur) {
    var entite = jpaRepository.save(mapper.versEntite(utilisateur));
    return mapper.versDomaine(entite);
  }

  @Override
  public Optional<Utilisateur> parId(UUID id) {
    return jpaRepository.findById(id).map(mapper::versDomaine);
  }

  @Override
  public Optional<Utilisateur> parLogin(String login) {
    return jpaRepository.findByLogin(login).map(mapper::versDomaine);
  }

  @Override
  public boolean existeParLogin(String login) {
    return jpaRepository.existsByLogin(login);
  }

  @Override
  public Page<Utilisateur> rechercher(String texteRecherche, PageRequest pageRequest) {
    var pageable =
        org.springframework.data.domain.PageRequest.of(pageRequest.numero(), pageRequest.taille());
    org.springframework.data.domain.Page<UtilisateurEntity> pageJpa =
        (texteRecherche == null || texteRecherche.isBlank())
            ? jpaRepository.findAll(pageable)
            : jpaRepository.findByLoginContainingIgnoreCaseOrEmailContainingIgnoreCase(
                texteRecherche, texteRecherche, pageable);

    var contenu = pageJpa.getContent().stream().map(mapper::versDomaine).toList();
    return Page.of(contenu, pageJpa.getNumber(), pageJpa.getSize(), pageJpa.getTotalElements());
  }
}
