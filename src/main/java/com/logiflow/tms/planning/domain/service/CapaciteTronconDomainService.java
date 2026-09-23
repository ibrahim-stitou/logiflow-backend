package com.logiflow.tms.planning.domain.service;

import com.logiflow.tms.planning.domain.model.ArretVoyage;
import com.logiflow.tms.shared.domain.vo.Capacite;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Calcule l'utilisation de capacité par tronçon (entre deux arrêts consécutifs) via un tableau de
 * différences, puis vérifie si un nouveau dossier tient dans la remorque sur chaque tronçon
 * concerné.
 */
public class CapaciteTronconDomainService {

  public record DossierSurTroncons(
      double poidsKg, double volumeM3, int indiceChargement, int indiceDechargement) {}

  public record UtilisationTroncon(
      int indiceTroncon,
      UUID arretDepartId,
      UUID arretArriveeId,
      double poidsUtiliseKg,
      double volumeUtiliseM3) {}

  public enum MotifDepassement {
    POIDS,
    VOLUME
  }

  public record TronconInsuffisant(
      UUID arretDepartId,
      UUID arretArriveeId,
      MotifDepassement motif,
      double depassementKg,
      double depassementM3) {}

  public record ResultatVerification(boolean compatible, List<TronconInsuffisant> tronconsDepasses) {}

  public List<UtilisationTroncon> calculerUtilisation(
      List<ArretVoyage> arrets, List<DossierSurTroncons> dossiers) {
    List<ArretVoyage> arretsOrdonnes =
        arrets.stream().sorted(Comparator.comparingInt(ArretVoyage::indiceSequence)).toList();
    if (arretsOrdonnes.size() < 2) {
      return List.of();
    }

    int nbArrets = arretsOrdonnes.size();
    double[] poidsDiff = new double[nbArrets];
    double[] volumeDiff = new double[nbArrets];

    for (DossierSurTroncons dossier : dossiers) {
      validerIndices(dossier.indiceChargement(), dossier.indiceDechargement(), nbArrets);
      poidsDiff[dossier.indiceChargement()] += dossier.poidsKg();
      poidsDiff[dossier.indiceDechargement()] -= dossier.poidsKg();
      volumeDiff[dossier.indiceChargement()] += dossier.volumeM3();
      volumeDiff[dossier.indiceDechargement()] -= dossier.volumeM3();
    }

    int nbTroncons = nbArrets - 1;
    List<UtilisationTroncon> resultat = new ArrayList<>(nbTroncons);
    double poidsCumul = 0;
    double volumeCumul = 0;
    for (int i = 0; i < nbTroncons; i++) {
      poidsCumul += poidsDiff[i];
      volumeCumul += volumeDiff[i];
      resultat.add(
          new UtilisationTroncon(
              i,
              arretsOrdonnes.get(i).id(),
              arretsOrdonnes.get(i + 1).id(),
              poidsCumul,
              volumeCumul));
    }
    return resultat;
  }

  public ResultatVerification verifierAjoutDossier(
      List<UtilisationTroncon> utilisationActuelle,
      Capacite capaciteMax,
      int indiceChargement,
      int indiceDechargement,
      double poidsKg,
      double volumeM3) {
    validerIndices(indiceChargement, indiceDechargement, utilisationActuelle.size() + 1);

    List<TronconInsuffisant> depassements = new ArrayList<>();
    for (UtilisationTroncon troncon : utilisationActuelle) {
      if (troncon.indiceTroncon() < indiceChargement
          || troncon.indiceTroncon() >= indiceDechargement) {
        continue;
      }
      double nouveauPoids = troncon.poidsUtiliseKg() + poidsKg;
      double nouveauVolume = troncon.volumeUtiliseM3() + volumeM3;
      if (nouveauPoids > capaciteMax.poidsKg()) {
        depassements.add(
            new TronconInsuffisant(
                troncon.arretDepartId(),
                troncon.arretArriveeId(),
                MotifDepassement.POIDS,
                nouveauPoids - capaciteMax.poidsKg(),
                0));
      }
      if (nouveauVolume > capaciteMax.volumeM3()) {
        depassements.add(
            new TronconInsuffisant(
                troncon.arretDepartId(),
                troncon.arretArriveeId(),
                MotifDepassement.VOLUME,
                0,
                nouveauVolume - capaciteMax.volumeM3()));
      }
    }
    return new ResultatVerification(depassements.isEmpty(), depassements);
  }

  private void validerIndices(int indiceChargement, int indiceDechargement, int nbArrets) {
    if (indiceChargement < 0 || indiceDechargement > nbArrets) {
      throw new IllegalArgumentException(
          "Les indices d'arrêt sont hors limites pour un itinéraire de " + nbArrets + " arrêts");
    }
    if (indiceChargement >= indiceDechargement) {
      throw new IllegalArgumentException(
          "L'arrêt de chargement doit précéder l'arrêt de déchargement sur l'itinéraire");
    }
  }
}
