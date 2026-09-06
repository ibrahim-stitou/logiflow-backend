package com.logiflow.tms.document.infrastructure.persistence.adapter;

import com.logiflow.tms.document.domain.model.Document;
import com.logiflow.tms.document.domain.model.TypeEntiteDocumentable;
import com.logiflow.tms.document.domain.port.out.DocumentRepository;
import com.logiflow.tms.document.infrastructure.persistence.mapper.DocumentMapper;
import com.logiflow.tms.document.infrastructure.persistence.repository.DocumentJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentRepositoryAdapter implements DocumentRepository {

  private final DocumentJpaRepository jpaRepository;
  private final DocumentMapper mapper;

  @Override
  public Document sauvegarder(Document document) {
    var entite = jpaRepository.save(mapper.versEntite(document));
    return mapper.versDomaine(entite);
  }

  @Override
  public Optional<Document> parId(UUID id) {
    return jpaRepository.findById(id).map(mapper::versDomaine);
  }

  @Override
  public List<Document> parEntite(TypeEntiteDocumentable typeEntite, UUID entiteId) {
    return jpaRepository.findByTypeEntiteAndEntiteId(typeEntite.name(), entiteId).stream()
        .map(mapper::versDomaine)
        .toList();
  }

  @Override
  public void supprimer(UUID id) {
    jpaRepository.deleteById(id);
  }
}
