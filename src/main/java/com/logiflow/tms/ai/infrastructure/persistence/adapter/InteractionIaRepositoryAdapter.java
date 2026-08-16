package com.logiflow.tms.ai.infrastructure.persistence.adapter;

import com.logiflow.tms.ai.domain.model.InteractionIa;
import com.logiflow.tms.ai.domain.port.out.InteractionIaRepository;
import com.logiflow.tms.ai.infrastructure.persistence.mapper.InteractionIaMapper;
import com.logiflow.tms.ai.infrastructure.persistence.repository.InteractionIaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InteractionIaRepositoryAdapter implements InteractionIaRepository {

  private final InteractionIaJpaRepository jpaRepository;
  private final InteractionIaMapper mapper;

  @Override
  public InteractionIa sauvegarder(InteractionIa interaction) {
    return mapper.versDomaine(jpaRepository.save(mapper.versEntite(interaction)));
  }
}
