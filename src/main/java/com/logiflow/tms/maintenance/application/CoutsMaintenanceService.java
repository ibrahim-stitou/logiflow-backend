package com.logiflow.tms.maintenance.application;

import com.logiflow.tms.maintenance.application.EnginsFlotte.EtatEngin;
import com.logiflow.tms.maintenance.domain.model.OrdreTravail;
import com.logiflow.tms.maintenance.domain.model.Sinistre;
import com.logiflow.tms.maintenance.domain.model.StatutOT;
import com.logiflow.tms.maintenance.domain.model.TypeEngin;
import com.logiflow.tms.maintenance.domain.port.out.OrdreTravailRepository;
import com.logiflow.tms.maintenance.domain.port.out.SinistreRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tableau des coûts de maintenance sur une période : OT terminés (par date de fin réelle), répartis
 * par type, nature, mois et engin, et sinistralité (coût net des sinistres survenus).
 */
@Service
@RequiredArgsConstructor
public class CoutsMaintenanceService {

  private static final int TOP_ENGINS = 10;

  private final OrdreTravailRepository ordreTravailRepository;
  private final SinistreRepository sinistreRepository;
  private final EnginsFlotte enginsFlotte;

  public record Repartition(String cle, String libelle, BigDecimal totalHt, long nombre) {}

  public record CoutsMaintenance(
      LocalDate debut,
      LocalDate fin,
      BigDecimal totalHt,
      BigDecimal totalTtc,
      long nombreOrdres,
      BigDecimal budgetEstime,
      List<Repartition> parType,
      List<Repartition> parNature,
      List<Repartition> parMois,
      List<Repartition> parEngin,
      long nombreSinistres,
      BigDecimal coutNetSinistres,
      BigDecimal indemnitesPercues) {}

  /** {@code fin} incluse ; {@code typeEngin} et {@code enginId} facultatifs. */
  @Transactional(readOnly = true)
  public CoutsMaintenance couts(LocalDate debut, LocalDate fin, TypeEngin typeEngin, UUID enginId) {
    List<OrdreTravail> termines =
        ordreTravailRepository
            .lister(
                new OrdreTravailRepository.Filtre(
                    typeEngin, enginId, StatutOT.TERMINE, null, null, null, null, null, null))
            .stream()
            .filter(o -> o.realisation().finReelle() != null)
            .filter(
                o -> {
                  LocalDate jour = o.realisation().finReelle().toLocalDate();
                  return !jour.isBefore(debut) && !jour.isAfter(fin);
                })
            .toList();

    Map<UUID, EtatEngin> etats = enginsFlotte.etats();
    BigDecimal totalHt = somme(termines, o -> o.totalHt().montant());
    BigDecimal totalTtc = somme(termines, o -> o.totalTtc().montant());
    BigDecimal budget =
        somme(
            termines,
            o ->
                o.details().budgetEstime() == null
                    ? BigDecimal.ZERO
                    : o.details().budgetEstime().montant());

    List<Sinistre> sinistres =
        sinistreRepository.lister(
            new SinistreRepository.Filtre(
                enginId,
                null,
                null,
                null,
                null,
                debut.atStartOfDay(),
                fin.plusDays(1).atStartOfDay()));
    List<Sinistre> sinistresFiltres =
        typeEngin == null
            ? sinistres
            : sinistres.stream()
                .filter(
                    s ->
                        typeEngin == TypeEngin.VEHICULE
                            ? s.circonstances().vehiculeId() != null
                            : s.circonstances().remorqueId() != null)
                .toList();
    BigDecimal coutNet = BigDecimal.ZERO;
    BigDecimal indemnites = BigDecimal.ZERO;
    for (Sinistre s : sinistresFiltres) {
      var couts = SinistreService.couts(s, ordreTravailRepository.parSinistreId(s.id()));
      coutNet = coutNet.add(couts.coutNet().montant());
      indemnites = indemnites.add(couts.indemnite().montant());
    }

    return new CoutsMaintenance(
        debut,
        fin,
        totalHt,
        totalTtc,
        termines.size(),
        budget,
        repartir(termines, o -> o.details().type().name(), Function.identity(), false),
        repartir(termines, o -> o.details().nature().name(), Function.identity(), false),
        repartir(
            termines,
            o -> YearMonth.from(o.realisation().finReelle()).toString(),
            Function.identity(),
            true),
        repartir(
                termines,
                o -> o.engin().id().toString(),
                id -> {
                  EtatEngin etat = etats.get(UUID.fromString(id));
                  return etat == null ? id : etat.immatriculation();
                },
                false)
            .stream()
            .limit(TOP_ENGINS)
            .toList(),
        sinistresFiltres.size(),
        coutNet,
        indemnites);
  }

  private static BigDecimal somme(
      List<OrdreTravail> ordres, Function<OrdreTravail, BigDecimal> montant) {
    return ordres.stream().map(montant).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /** Regroupe par clé ; trié par clé (chronologique) ou par montant décroissant. */
  private static List<Repartition> repartir(
      List<OrdreTravail> ordres,
      Function<OrdreTravail, String> cle,
      Function<String, String> libelle,
      boolean parCle) {
    Map<String, List<OrdreTravail>> groupes =
        ordres.stream()
            .collect(
                Collectors.groupingBy(
                    cle, parCle ? TreeMap::new : LinkedHashMap::new, Collectors.toList()));
    Comparator<Repartition> ordre =
        parCle
            ? Comparator.comparing(Repartition::cle)
            : Comparator.comparing(Repartition::totalHt).reversed();
    return groupes.entrySet().stream()
        .map(
            e ->
                new Repartition(
                    e.getKey(),
                    libelle.apply(e.getKey()),
                    somme(e.getValue(), o -> o.totalHt().montant()),
                    e.getValue().size()))
        .sorted(ordre)
        .toList();
  }
}
