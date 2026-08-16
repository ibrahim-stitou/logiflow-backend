package com.logiflow.tms.referential.infrastructure.persistence.adapter;

import com.logiflow.tms.referential.domain.model.Client;
import com.logiflow.tms.referential.domain.port.out.ClientRepository;
import com.logiflow.tms.referential.infrastructure.persistence.mapper.ClientMapper;
import com.logiflow.tms.referential.infrastructure.persistence.repository.ClientJpaRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientRepositoryAdapter implements ClientRepository {

  private final ClientJpaRepository jpaRepository;
  private final ClientMapper mapper;

  @Override
  public Client sauvegarder(Client client) {
    var entite = jpaRepository.save(mapper.versEntite(client));
    return mapper.versDomaine(entite);
  }

  @Override
  public Optional<Client> parId(UUID id) {
    return jpaRepository.findById(id).map(mapper::versDomaine);
  }

  @Override
  public Optional<Client> parCode(String code) {
    return jpaRepository.findByCode(code).map(mapper::versDomaine);
  }

  @Override
  public boolean existeParCode(String code) {
    return jpaRepository.existsByCode(code);
  }
}
