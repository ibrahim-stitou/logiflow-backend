package com.logiflow.tms.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.logiflow.tms.order.application.command.CreerCommandeCommand;
import com.logiflow.tms.order.domain.model.Commande;
import com.logiflow.tms.order.domain.port.out.CommandeRepository;
import com.logiflow.tms.order.domain.port.out.SequenceReferenceGenerator;
import com.logiflow.tms.order.domain.service.OrderDomainService;
import com.logiflow.tms.referential.api.ClientApi;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import com.logiflow.tms.shared.domain.vo.Money;
import com.logiflow.tms.shared.domain.vo.Reference;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommandeServiceTest {

  @Mock private CommandeRepository commandeRepository;
  @Mock private SequenceReferenceGenerator referenceGenerator;
  @Mock private ClientApi clientApi;

  private CommandeService commandeService;

  @BeforeEach
  void setUp() {
    commandeService =
        new CommandeService(
            commandeRepository, new OrderDomainService(), referenceGenerator, clientApi);
  }

  @Test
  void creerCommandeEchoueSiLeClientNEstPasActif() {
    UUID clientId = UUID.randomUUID();
    CreerCommandeCommand command =
        new CreerCommandeCommand(
            clientId,
            LocalDate.now().plusDays(3),
            new Money(BigDecimal.TEN, Currency.getInstance("EUR")));
    when(clientApi.estActif(clientId)).thenReturn(false);

    assertThatThrownBy(() -> commandeService.creerCommande(command))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void creerCommandeSauvegardeUneNouvelleCommande() {
    UUID clientId = UUID.randomUUID();
    CreerCommandeCommand command =
        new CreerCommandeCommand(
            clientId,
            LocalDate.now().plusDays(3),
            new Money(BigDecimal.TEN, Currency.getInstance("EUR")));
    when(clientApi.estActif(clientId)).thenReturn(true);
    when(referenceGenerator.generer(anyString(), anyInt()))
        .thenReturn(Reference.generer("CMD", 2026, 1));
    when(commandeRepository.sauvegarder(any(Commande.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UUID id = commandeService.creerCommande(command);

    assertThat(id).isNotNull();
  }
}
