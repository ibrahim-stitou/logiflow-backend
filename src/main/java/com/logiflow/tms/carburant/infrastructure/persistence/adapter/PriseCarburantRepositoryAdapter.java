package com.logiflow.tms.carburant.infrastructure.persistence.adapter;

import com.logiflow.tms.carburant.domain.model.PriseCarburant;
import com.logiflow.tms.carburant.domain.model.StatutPrise;
import com.logiflow.tms.carburant.domain.model.TypeCarburant;
import com.logiflow.tms.carburant.domain.port.out.PriseCarburantRepository;
import com.logiflow.tms.carburant.infrastructure.persistence.mapper.PriseCarburantMapper;
import com.logiflow.tms.carburant.infrastructure.persistence.repository.PriseCarburantJpaRepository;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PriseCarburantRepositoryAdapter implements PriseCarburantRepository {

  private final PriseCarburantJpaRepository jpaRepository;
  private final PriseCarburantMapper mapper;

  @Override
  public PriseCarburant sauvegarder(PriseCarburant prise) {
    var entite =
        jpaRepository
            .findById(prise.id())
            .map(
                existante -> {
                  mapper.mettreAJour(existante, prise);
                  return existante;
                })
            .orElseGet(() -> mapper.versEntite(prise));
    return mapper.versDomaine(jpaRepository.save(entite));
  }

  @Override
  public Optional<PriseCarburant> parId(UUID id) {
    return jpaRepository.findById(id).map(mapper::versDomaine);
  }

  @Override
  public Page<PriseCarburant> rechercher(
      String texteRecherche, UUID voyageId, StatutPrise statut, PageRequest pageRequest) {
    var pageable =
        org.springframework.data.domain.PageRequest.of(pageRequest.numero(), pageRequest.taille());
    var pageJpa =
        jpaRepository.rechercher(
            texteRecherche, voyageId, statut != null ? statut.name() : null, pageable);
    var contenu = pageJpa.getContent().stream().map(mapper::versDomaine).toList();
    return Page.of(contenu, pageJpa.getNumber(), pageJpa.getSize(), pageJpa.getTotalElements());
  }

  @Override
  public PriseCarburantStats stats(String texteRecherche, UUID voyageId, StatutPrise statut) {
    String statutNom = statut != null ? statut.name() : null;
    return versStats(
        jpaRepository.agregerTotaux(texteRecherche, voyageId, statutNom),
        jpaRepository.agregerParType(texteRecherche, voyageId, statutNom));
  }

  @Override
  public PriseCarburantStats statsPeriode(UUID vehiculeId, Instant debut, Instant fin) {
    return versStats(
        jpaRepository.agregerTotauxPeriode(vehiculeId, debut, fin),
        jpaRepository.agregerParTypePeriode(vehiculeId, debut, fin));
  }

  private static PriseCarburantStats versStats(Object[] totaux, List<Object[]> lignesParType) {
    // Une requête agrégée JPQL à plusieurs colonnes peut revenir enveloppée dans un Object[].
    if (totaux.length == 1 && totaux[0] instanceof Object[] imbrique) {
      totaux = imbrique;
    }
    long nombre = totaux[0] != null ? ((Number) totaux[0]).longValue() : 0L;
    double litres = totaux[1] != null ? ((Number) totaux[1]).doubleValue() : 0.0;
    BigDecimal montant = totaux[2] != null ? (BigDecimal) totaux[2] : BigDecimal.ZERO;

    List<ParType> parType =
        lignesParType.stream()
            .map(
                row ->
                    new ParType(
                        TypeCarburant.valueOf((String) row[0]),
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).doubleValue(),
                        (BigDecimal) row[3]))
            .toList();

    return new PriseCarburantStats(nombre, litres, montant, parType);
  }
}
