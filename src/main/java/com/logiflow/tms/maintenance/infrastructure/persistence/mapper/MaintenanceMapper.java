package com.logiflow.tms.maintenance.infrastructure.persistence.mapper;

import com.logiflow.tms.maintenance.domain.model.ContratAssurance;
import com.logiflow.tms.maintenance.domain.model.Garantie;
import com.logiflow.tms.maintenance.domain.model.GraviteSinistre;
import com.logiflow.tms.maintenance.domain.model.NatureIntervention;
import com.logiflow.tms.maintenance.domain.model.OrdreTravail;
import com.logiflow.tms.maintenance.domain.model.OrigineOT;
import com.logiflow.tms.maintenance.domain.model.PlanEntretien;
import com.logiflow.tms.maintenance.domain.model.Prestataire;
import com.logiflow.tms.maintenance.domain.model.PrioriteOT;
import com.logiflow.tms.maintenance.domain.model.Responsabilite;
import com.logiflow.tms.maintenance.domain.model.Sinistre;
import com.logiflow.tms.maintenance.domain.model.StatutOT;
import com.logiflow.tms.maintenance.domain.model.StatutSinistre;
import com.logiflow.tms.maintenance.domain.model.TypeContrat;
import com.logiflow.tms.maintenance.domain.model.TypeEngin;
import com.logiflow.tms.maintenance.domain.model.TypeIntervention;
import com.logiflow.tms.maintenance.domain.model.TypePrestataire;
import com.logiflow.tms.maintenance.domain.model.TypeSinistre;
import com.logiflow.tms.maintenance.domain.vo.EnginRef;
import com.logiflow.tms.maintenance.domain.vo.LigneCout;
import com.logiflow.tms.maintenance.domain.vo.Tiers;
import com.logiflow.tms.maintenance.infrastructure.persistence.entity.ContratAssuranceEntity;
import com.logiflow.tms.maintenance.infrastructure.persistence.entity.OrdreTravailEntity;
import com.logiflow.tms.maintenance.infrastructure.persistence.entity.PlanEntretienEntity;
import com.logiflow.tms.maintenance.infrastructure.persistence.entity.PrestataireEntity;
import com.logiflow.tms.maintenance.infrastructure.persistence.entity.SinistreEntity;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import com.logiflow.tms.shared.domain.vo.Money;
import com.logiflow.tms.shared.domain.vo.Reference;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Traduit entre le modèle de domaine du module maintenance et ses entités JPA. Les méthodes {@code
 * appliquer} remplissent une entité neuve ou existante (même code pour créer et mettre à jour) ;
 * les montants sont stockés en euros ({@link OrdreTravail#DEVISE}).
 */
@Component
@RequiredArgsConstructor
public class MaintenanceMapper {

  private final ObjectMapper objectMapper;

  // ─── Ordre de travail ──────────────────────────────────────────────────

  public void appliquer(OrdreTravailEntity e, OrdreTravail ot) {
    var d = ot.details();
    var r = ot.realisation();
    e.setReference(ot.reference().valeur());
    e.setTypeEngin(ot.engin().type().name());
    e.setEnginId(ot.engin().id());
    e.setOrigine(ot.origine().name());
    e.setPlanId(ot.planId());
    e.setSinistreId(ot.sinistreId());
    e.setTypeIntervention(d.type().name());
    e.setNature(d.nature().name());
    e.setPriorite(d.priorite().name());
    e.setTitre(d.titre());
    e.setDescription(d.description());
    e.setPrestataireId(d.prestataireId());
    e.setDebutPlanifie(d.debutPlanifie());
    e.setFinPlanifiee(d.finPlanifiee());
    e.setImmobilisation(d.immobilisation());
    e.setBudgetEstime(montant(d.budgetEstime()));
    e.setStatut(ot.statut().name());
    e.setLignesJson(versJson(ot.lignes()));
    e.setTotalHt(ot.totalHt().montant());
    e.setTotalTtc(ot.totalTtc().montant());
    e.setDebutReel(r.debutReel());
    e.setFinReelle(r.finReelle());
    e.setKilometrage(r.kilometrage());
    e.setHeures(r.heures());
    e.setDiagnostic(r.diagnostic());
    e.setTravauxRealises(r.travauxRealises());
    e.setIntervenant(r.intervenant());
    e.setNumeroFacture(r.numeroFacture());
    e.setDateFacture(r.dateFacture());
  }

  public OrdreTravail versDomaine(OrdreTravailEntity e) {
    return OrdreTravail.reconstituer(
        e.getId(),
        new Reference(e.getReference()),
        new EnginRef(TypeEngin.valueOf(e.getTypeEngin()), e.getEnginId()),
        OrigineOT.valueOf(e.getOrigine()),
        e.getPlanId(),
        e.getSinistreId(),
        new OrdreTravail.DetailsOT(
            TypeIntervention.valueOf(e.getTypeIntervention()),
            NatureIntervention.valueOf(e.getNature()),
            PrioriteOT.valueOf(e.getPriorite()),
            e.getTitre(),
            e.getDescription(),
            e.getPrestataireId(),
            e.getDebutPlanifie(),
            e.getFinPlanifiee(),
            e.isImmobilisation(),
            money(e.getBudgetEstime())),
        StatutOT.valueOf(e.getStatut()),
        lire(e.getLignesJson(), new TypeReference<List<LigneCout>>() {}),
        new OrdreTravail.Realisation(
            e.getDebutReel(),
            e.getFinReelle(),
            e.getKilometrage(),
            e.getHeures(),
            e.getDiagnostic(),
            e.getTravauxRealises(),
            e.getIntervenant(),
            e.getNumeroFacture(),
            e.getDateFacture()));
  }

  // ─── Plan d'entretien ──────────────────────────────────────────────────

  public void appliquer(PlanEntretienEntity e, PlanEntretien plan) {
    var p = plan.parametres();
    e.setTypeEngin(plan.engin().type().name());
    e.setEnginId(plan.engin().id());
    e.setLibelle(p.libelle());
    e.setTypeIntervention(p.type().name());
    e.setPeriodiciteKm(p.periodiciteKm());
    e.setPeriodiciteMois(p.periodiciteMois());
    e.setPeriodiciteHeures(p.periodiciteHeures());
    e.setSeuilAlerteKm(p.seuilAlerteKm());
    e.setSeuilAlerteJours(p.seuilAlerteJours());
    e.setDureeEstimeeMin(p.dureeEstimeeMin());
    e.setCoutEstime(montant(p.coutEstime()));
    e.setPrestataireId(p.prestataireId());
    e.setActif(p.actif());
    var derniere = plan.derniereRealisation();
    e.setDerniereDate(derniere == null ? null : derniere.date());
    e.setDerniereKm(derniere == null ? null : derniere.kilometrage());
    e.setDerniereHeures(derniere == null ? null : derniere.heures());
  }

  public PlanEntretien versDomaine(PlanEntretienEntity e) {
    return PlanEntretien.reconstituer(
        e.getId(),
        new EnginRef(TypeEngin.valueOf(e.getTypeEngin()), e.getEnginId()),
        new PlanEntretien.Parametres(
            e.getLibelle(),
            TypeIntervention.valueOf(e.getTypeIntervention()),
            e.getPeriodiciteKm(),
            e.getPeriodiciteMois(),
            e.getPeriodiciteHeures(),
            e.getSeuilAlerteKm(),
            e.getSeuilAlerteJours(),
            e.getDureeEstimeeMin(),
            money(e.getCoutEstime()),
            e.getPrestataireId(),
            e.isActif()),
        e.getDerniereDate() == null
            ? null
            : new PlanEntretien.DerniereRealisation(
                e.getDerniereDate(), e.getDerniereKm(), e.getDerniereHeures()));
  }

  // ─── Sinistre ──────────────────────────────────────────────────────────

  public void appliquer(SinistreEntity e, Sinistre s) {
    var c = s.circonstances();
    var a = s.assurance();
    e.setReference(s.reference().valeur());
    e.setVehiculeId(c.vehiculeId());
    e.setRemorqueId(c.remorqueId());
    e.setChauffeurId(c.chauffeurId());
    e.setVoyageId(c.voyageId());
    e.setDateSurvenance(c.dateSurvenance());
    e.setLieu(c.lieu());
    e.setLatitude(c.position() == null ? null : c.position().latitude());
    e.setLongitude(c.position() == null ? null : c.position().longitude());
    e.setTypeSinistre(c.type().name());
    e.setGravite(c.gravite().name());
    e.setResponsabilite(c.responsabilite().name());
    e.setDescription(c.description());
    e.setConstatAmiable(c.constatAmiable());
    e.setRapportPolice(c.rapportPolice());
    e.setBlesses(c.blesses());
    e.setEnginImmobilise(c.enginImmobilise());
    Tiers t = c.tiers();
    e.setTiersNom(t == null ? null : t.nom());
    e.setTiersImmatriculation(t == null ? null : t.immatriculation());
    e.setTiersAssureur(t == null ? null : t.assureur());
    e.setTiersNumeroPolice(t == null ? null : t.numeroPolice());
    e.setContratId(a.contratId());
    e.setNumeroDossierAssureur(a.numeroDossierAssureur());
    e.setDateDeclarationAssureur(a.dateDeclarationAssureur());
    e.setExpertId(a.expertId());
    e.setDateExpertise(a.dateExpertise());
    e.setEstimationDommages(montant(a.estimationDommages()));
    e.setFranchise(montant(a.franchise()));
    e.setIndemnite(montant(a.indemnite()));
    e.setStatut(s.statut().name());
    e.setDateCloture(s.dateCloture());
  }

  public Sinistre versDomaine(SinistreEntity e) {
    return Sinistre.reconstituer(
        e.getId(),
        new Reference(e.getReference()),
        new Sinistre.Circonstances(
            e.getVehiculeId(),
            e.getRemorqueId(),
            e.getChauffeurId(),
            e.getVoyageId(),
            e.getDateSurvenance(),
            e.getLieu(),
            e.getLatitude() == null || e.getLongitude() == null
                ? null
                : new GeoPoint(e.getLatitude(), e.getLongitude()),
            TypeSinistre.valueOf(e.getTypeSinistre()),
            GraviteSinistre.valueOf(e.getGravite()),
            Responsabilite.valueOf(e.getResponsabilite()),
            e.getDescription(),
            e.isConstatAmiable(),
            e.isRapportPolice(),
            e.isBlesses(),
            e.isEnginImmobilise(),
            e.getTiersNom() == null
                ? null
                : new Tiers(
                    e.getTiersNom(),
                    e.getTiersImmatriculation(),
                    e.getTiersAssureur(),
                    e.getTiersNumeroPolice())),
        new Sinistre.SuiviAssurance(
            e.getContratId(),
            e.getNumeroDossierAssureur(),
            e.getDateDeclarationAssureur(),
            e.getExpertId(),
            e.getDateExpertise(),
            money(e.getEstimationDommages()),
            money(e.getFranchise()),
            money(e.getIndemnite())),
        StatutSinistre.valueOf(e.getStatut()),
        e.getDateCloture());
  }

  // ─── Prestataire ───────────────────────────────────────────────────────

  public void appliquer(PrestataireEntity e, Prestataire p) {
    var f = p.fiche();
    e.setCode(p.code());
    e.setRaisonSociale(f.raisonSociale());
    e.setTypePrestataire(f.type().name());
    e.setSiret(f.siret());
    e.setContactNom(f.contactNom());
    e.setTelephone(f.telephone());
    e.setEmail(f.email());
    e.setAdresse(f.adresse());
    e.setNotes(f.notes());
    e.setActif(f.actif());
  }

  public Prestataire versDomaine(PrestataireEntity e) {
    return Prestataire.reconstituer(
        e.getId(),
        e.getCode(),
        new Prestataire.Fiche(
            e.getRaisonSociale(),
            TypePrestataire.valueOf(e.getTypePrestataire()),
            e.getSiret(),
            e.getContactNom(),
            e.getTelephone(),
            e.getEmail(),
            e.getAdresse(),
            e.getNotes(),
            e.isActif()));
  }

  // ─── Contrat d'assurance ───────────────────────────────────────────────

  public void appliquer(ContratAssuranceEntity e, ContratAssurance contrat) {
    var c = contrat.conditions();
    e.setAssureurId(c.assureurId());
    e.setNumeroPolice(c.numeroPolice());
    e.setTypeContrat(c.type().name());
    e.setGarantiesJson(versJson(c.garanties()));
    e.setFranchise(montant(c.franchise()));
    e.setPrimeAnnuelle(montant(c.primeAnnuelle()));
    e.setDateEffet(c.dateEffet());
    e.setDateEcheance(c.dateEcheance());
    e.setEnginsJson(versJson(c.engins()));
    e.setActif(c.actif());
  }

  public ContratAssurance versDomaine(ContratAssuranceEntity e) {
    return ContratAssurance.reconstituer(
        e.getId(),
        new ContratAssurance.Conditions(
            e.getAssureurId(),
            e.getNumeroPolice(),
            TypeContrat.valueOf(e.getTypeContrat()),
            lire(e.getGarantiesJson(), new TypeReference<Set<Garantie>>() {}),
            money(e.getFranchise()),
            money(e.getPrimeAnnuelle()),
            e.getDateEffet(),
            e.getDateEcheance(),
            lire(e.getEnginsJson(), new TypeReference<List<EnginRef>>() {}),
            e.isActif()));
  }

  // ─── Utilitaires ───────────────────────────────────────────────────────

  private static BigDecimal montant(Money money) {
    return money == null ? null : money.montant();
  }

  private static Money money(BigDecimal montant) {
    return montant == null ? null : new Money(montant, OrdreTravail.DEVISE);
  }

  private String versJson(Object valeur) {
    try {
      return objectMapper.writeValueAsString(valeur);
    } catch (JacksonException e) {
      throw new IllegalStateException("Échec de sérialisation JSON en persistance", e);
    }
  }

  private <T> T lire(String json, TypeReference<T> type) {
    try {
      return objectMapper.readValue(json, type);
    } catch (JacksonException e) {
      throw new IllegalStateException("Échec de désérialisation JSON en persistance", e);
    }
  }
}
