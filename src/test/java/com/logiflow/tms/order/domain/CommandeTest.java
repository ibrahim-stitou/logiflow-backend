package com.logiflow.tms.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.logiflow.tms.order.domain.model.Commande;
import com.logiflow.tms.order.domain.model.StatutCommande;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.domain.vo.Money;
import com.logiflow.tms.shared.domain.vo.Reference;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CommandeTest {

  private static final Reference REFERENCE = Reference.generer("CMD", 2026, 1);
  private static final Money PRIX =
      new Money(BigDecimal.valueOf(1200), Currency.getInstance("EUR"));

  @Test
  void creerUneCommandeEstAuStatutRecue() {
    Commande commande =
        Commande.creer(
            UUID.randomUUID(), REFERENCE, UUID.randomUUID(), LocalDate.now().plusDays(5), PRIX);

    assertThat(commande.statut()).isEqualTo(StatutCommande.RECUE);
    assertThat(commande.estConfirmee()).isFalse();
  }

  @Test
  void confirmerUneCommandeRecuePasseAuStatutConfirmee() {
    Commande commande =
        Commande.creer(
            UUID.randomUUID(), REFERENCE, UUID.randomUUID(), LocalDate.now().plusDays(5), PRIX);

    commande.confirmer();

    assertThat(commande.estConfirmee()).isTrue();
  }

  @Test
  void confirmerUneCommandeDejaConfirmeeEchoue() {
    Commande commande =
        Commande.creer(
            UUID.randomUUID(), REFERENCE, UUID.randomUUID(), LocalDate.now().plusDays(5), PRIX);
    commande.confirmer();

    assertThatThrownBy(commande::confirmer).isInstanceOf(BusinessException.class);
  }

  @Test
  void annulerUneCommandeDejaAnnuleeEchoue() {
    Commande commande =
        Commande.creer(
            UUID.randomUUID(), REFERENCE, UUID.randomUUID(), LocalDate.now().plusDays(5), PRIX);
    commande.annuler();

    assertThatThrownBy(commande::annuler).isInstanceOf(BusinessException.class);
  }
}
