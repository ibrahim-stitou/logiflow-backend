package com.logiflow.tms.order.domain.model;

import com.logiflow.tms.order.domain.vo.LigneCommande;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.domain.vo.Money;
import com.logiflow.tms.shared.domain.vo.Reference;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Commande client : demande commerciale de transport. Entité racine du module {@code order}. */
public final class Commande {

  private final UUID id;
  private final Reference reference;
  private final UUID clientId;
  private StatutCommande statut;
  private LocalDate dateSouhaitee;
  private Money prixNegocie;
  private List<LigneCommande> lignes;

  private Commande(
      UUID id,
      Reference reference,
      UUID clientId,
      StatutCommande statut,
      LocalDate dateSouhaitee,
      Money prixNegocie,
      List<LigneCommande> lignes) {
    this.id = Objects.requireNonNull(id, "L'identifiant de la commande est obligatoire");
    this.reference =
        Objects.requireNonNull(reference, "La référence de la commande est obligatoire");
    this.clientId = Objects.requireNonNull(clientId, "Le client est obligatoire");
    this.statut = Objects.requireNonNull(statut, "Le statut est obligatoire");
    this.dateSouhaitee = Objects.requireNonNull(dateSouhaitee, "La date souhaitée est obligatoire");
    this.prixNegocie = Objects.requireNonNull(prixNegocie, "Le prix négocié est obligatoire");
    this.lignes = List.copyOf(lignes);
    if (this.lignes.isEmpty()) {
      throw new IllegalArgumentException(
          "Une commande doit contenir au moins une ligne de marchandise");
    }
  }

  public static Commande creer(
      UUID id,
      Reference reference,
      UUID clientId,
      LocalDate dateSouhaitee,
      Money prixNegocie,
      List<LigneCommande> lignes) {
    return new Commande(
        id, reference, clientId, StatutCommande.RECUE, dateSouhaitee, prixNegocie, lignes);
  }

  public static Commande reconstituer(
      UUID id,
      Reference reference,
      UUID clientId,
      StatutCommande statut,
      LocalDate dateSouhaitee,
      Money prixNegocie,
      List<LigneCommande> lignes) {
    return new Commande(id, reference, clientId, statut, dateSouhaitee, prixNegocie, lignes);
  }

  public void confirmer() {
    if (statut != StatutCommande.RECUE) {
      throw new BusinessException(
          "Seule une commande au statut RECUE peut être confirmée (statut actuel : "
              + statut
              + ")");
    }
    this.statut = StatutCommande.CONFIRMEE;
  }

  public void annuler() {
    if (statut == StatutCommande.ANNULEE) {
      throw new BusinessException("La commande " + reference.valeur() + " est déjà annulée");
    }
    this.statut = StatutCommande.ANNULEE;
  }

  public void renegocierPrix(Money nouveauPrix) {
    this.prixNegocie = Objects.requireNonNull(nouveauPrix, "Le prix négocié est obligatoire");
  }

  public void mettreAJourLignes(List<LigneCommande> lignes) {
    List<LigneCommande> copie = List.copyOf(lignes);
    if (copie.isEmpty()) {
      throw new IllegalArgumentException(
          "Une commande doit contenir au moins une ligne de marchandise");
    }
    this.lignes = copie;
  }

  public boolean estConfirmee() {
    return statut == StatutCommande.CONFIRMEE;
  }

  public UUID id() {
    return id;
  }

  public Reference reference() {
    return reference;
  }

  public UUID clientId() {
    return clientId;
  }

  public StatutCommande statut() {
    return statut;
  }

  public LocalDate dateSouhaitee() {
    return dateSouhaitee;
  }

  public Money prixNegocie() {
    return prixNegocie;
  }

  public List<LigneCommande> lignes() {
    return lignes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Commande commande)) {
      return false;
    }
    return id.equals(commande.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
