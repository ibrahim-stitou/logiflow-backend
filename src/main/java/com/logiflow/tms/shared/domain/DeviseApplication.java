package com.logiflow.tms.shared.domain;

import java.util.Currency;

/** Devise par défaut de l'application (montants saisis et affichés en dirham marocain). */
public final class DeviseApplication {

  public static final Currency PAR_DEFAUT = Currency.getInstance("MAD");

  private DeviseApplication() {}
}
