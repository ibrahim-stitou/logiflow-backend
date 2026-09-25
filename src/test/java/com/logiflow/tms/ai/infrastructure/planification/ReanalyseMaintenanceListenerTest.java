package com.logiflow.tms.ai.infrastructure.planification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.logiflow.tms.ai.application.AnalyseMaintenanceAutomatique;
import com.logiflow.tms.document.api.DocumentEntiteModificationEvent;
import com.logiflow.tms.maintenance.api.EtatMaintenanceEnginModifieEvent;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReanalyseMaintenanceListenerTest {

  @Mock private AnalyseMaintenanceAutomatique analyse;

  private ReanalyseMaintenanceListener listener;

  @BeforeEach
  void preparer() {
    listener =
        new ReanalyseMaintenanceListener(
            analyse, new MaintenanceAutomatiqueProperties(true, "0 0 5 * * *", true, 30));
  }

  @Test
  void unChangementDEtatDeMaintenanceReanalyseLEngin() {
    UUID remorqueId = UUID.randomUUID();

    listener.surEtatMaintenanceModifie(
        new EtatMaintenanceEnginModifieEvent("REMORQUE", remorqueId, "Clôture de l'OT OT-1"));

    verify(analyse).reanalyserEngin(remorqueId, "Clôture de l'OT OT-1", 30);
  }

  @Test
  void seulsLesDocumentsDesVehiculesEtRemorquesDeclenchentUneReanalyse() {
    UUID vehiculeId = UUID.randomUUID();

    listener.surDocumentModifie(new DocumentEntiteModificationEvent("VEHICULE", vehiculeId));
    listener.surDocumentModifie(
        new DocumentEntiteModificationEvent("CHAUFFEUR", UUID.randomUUID()));

    verify(analyse).reanalyserEngin(eq(vehiculeId), anyString(), eq(30));
    verify(analyse, never())
        .reanalyserEngin(argThat(id -> !vehiculeId.equals(id)), any(), anyInt());
  }
}
