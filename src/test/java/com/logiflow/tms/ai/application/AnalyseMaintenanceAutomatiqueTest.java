package com.logiflow.tms.ai.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logiflow.tms.ai.application.MaintenancePredictiveService.AnalyserMaintenanceCommand;
import com.logiflow.tms.ai.domain.model.maintenance.ResultatMaintenance;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import com.logiflow.tms.shared.domain.exception.ServiceIndisponibleException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalyseMaintenanceAutomatiqueTest {

  @Mock private MaintenancePredictiveService service;

  @Test
  void lAnalyseDeNuitEnregistreLesScoresDeToutLaFlotte() {
    when(service.analyser(any())).thenReturn(new ResultatMaintenance(List.of(), "RAS", "GABARIT"));

    var resultat = new AnalyseMaintenanceAutomatique(service).analyserFlotte(30);

    assertThat(resultat).isPresent();
    verify(service).analyser(new AnalyserMaintenanceCommand(null, 30, true));
  }

  @Test
  void unServiceIaIndisponibleNeFaitPasEchouerLeDeclenchement() {
    when(service.analyser(any())).thenThrow(new ServiceIndisponibleException("IA indisponible"));
    var analyse = new AnalyseMaintenanceAutomatique(service);

    assertThat(analyse.analyserFlotte(30)).isEmpty();
    // Le verrou est libéré : la prochaine exécution repart.
    assertThat(analyse.analyserFlotte(30)).isEmpty();
    verify(service, times(2)).analyser(any());
  }

  @Test
  void laReanalyseDUnEnginHorsServiceEstIgnoree() {
    UUID enginId = UUID.randomUUID();
    when(service.analyser(any())).thenThrow(new NotFoundException("hors service"));

    new AnalyseMaintenanceAutomatique(service).reanalyserEngin(enginId, "test", 30);

    verify(service).analyser(new AnalyserMaintenanceCommand(enginId, 30, true));
  }

  @Test
  void deuxEvenementsSimultanesNeLancentQuUneReanalyseDuMemeEngin() throws Exception {
    UUID enginId = UUID.randomUUID();
    CountDownLatch demarree = new CountDownLatch(1);
    CountDownLatch liberer = new CountDownLatch(1);
    when(service.analyser(any()))
        .thenAnswer(
            invocation -> {
              demarree.countDown();
              liberer.await(5, TimeUnit.SECONDS);
              return new ResultatMaintenance(List.of(), "", "GABARIT");
            });
    var analyse = new AnalyseMaintenanceAutomatique(service);

    Thread premier = Thread.ofVirtual().start(() -> analyse.reanalyserEngin(enginId, "a", 30));
    assertThat(demarree.await(5, TimeUnit.SECONDS)).isTrue();
    analyse.reanalyserEngin(enginId, "b", 30);
    liberer.countDown();
    premier.join();

    verify(service, times(1)).analyser(any());
  }
}
