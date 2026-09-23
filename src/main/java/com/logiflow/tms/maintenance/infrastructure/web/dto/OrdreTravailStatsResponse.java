package com.logiflow.tms.maintenance.infrastructure.web.dto;

import com.logiflow.tms.maintenance.domain.port.out.OrdreTravailRepository;
import com.logiflow.tms.shared.domain.DeviseApplication;
import com.logiflow.tms.shared.domain.vo.Money;

public record OrdreTravailStatsResponse(long nombre, Money coutTotal, long enCours) {

  public static OrdreTravailStatsResponse depuis(OrdreTravailRepository.OrdreTravailStats stats) {
    return new OrdreTravailStatsResponse(
        stats.nombre(),
        new Money(stats.coutTotal(), DeviseApplication.PAR_DEFAUT),
        stats.enCours());
  }
}
