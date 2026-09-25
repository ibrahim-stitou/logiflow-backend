package com.logiflow.tms.carburant.infrastructure.persistence.adapter;

import static com.logiflow.tms.shared.infrastructure.persistence.JpaTupleAgregat.versBigDecimal;
import static com.logiflow.tms.shared.infrastructure.persistence.JpaTupleAgregat.versEntier;
import static com.logiflow.tms.shared.infrastructure.persistence.JpaTupleAgregat.versReel;

import com.logiflow.tms.carburant.domain.model.PriseCarburant;
import com.logiflow.tms.carburant.domain.model.StatutPrise;
import com.logiflow.tms.carburant.domain.model.TypeCarburant;
import com.logiflow.tms.carburant.domain.port.out.PriseCarburantRepository;
import com.logiflow.tms.carburant.infrastructure.persistence.mapper.PriseCarburantMapper;
import com.logiflow.tms.carburant.infrastructure.persistence.repository.PriseCarburantJpaRepository;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.infrastructure.persistence.JpaTupleAgregat;
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
        jpaRepository.agregerTotaux(texteRecherche, voyageId, statutNom).stream()
            .findFirst()
            .orElse(null),
        jpaRepository.agregerParType(texteRecherche, voyageId, statutNom));
  }

  @Override
  public PriseCarburantStats statsPeriode(UUID vehiculeId, Instant debut, Instant fin) {
    return versStats(
        jpaRepository.agregerTotauxPeriode(vehiculeId, debut, fin),
        jpaRepository.agregerParTypePeriode(vehiculeId, debut, fin));
  }

  /**
   * Totaux (nombre, litres, montant) et répartition par type. Les tuples agrégés JPQL sont
   * normalisés par {@link JpaTupleAgregat} (ligne simple ou enveloppée selon Hibernate).
   */
  private static PriseCarburantStats versStats(Object totauxBruts, List<Object[]> lignesParType) {
    Object[] totaux = JpaTupleAgregat.normaliserLigne(totauxBruts);
    long nombre = 0L;
    double litres = 0.0;
    BigDecimal montant = BigDecimal.ZERO;
    if (totaux != null) {
      nombre = versEntier(totaux[0]);
      litres = versReel(totaux[1]);
      montant = versBigDecimal(totaux[2]);
    }

    List<ParType> parType =
        lignesParType.stream()
            .map(JpaTupleAgregat::normaliserLigne)
            .map(
                row ->
                    new ParType(
                        versTypeCarburant(row[0]),
                        versEntier(row[1]),
                        versReel(row[2]),
                        versBigDecimal(row[3])))
            .toList();

    return new PriseCarburantStats(nombre, litres, montant, parType);
  }

  private static TypeCarburant versTypeCarburant(Object value) {
    if (value instanceof TypeCarburant type) {
      return type;
    }
    if (value instanceof String name) {
      return TypeCarburant.valueOf(name);
    }
    throw new IllegalStateException("Type carburant inattendu: " + value.getClass().getName());
  }
}
