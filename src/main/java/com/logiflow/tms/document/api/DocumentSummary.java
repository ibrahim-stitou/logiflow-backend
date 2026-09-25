package com.logiflow.tms.document.api;

import java.time.LocalDate;

/** Vue publique d'un document rattaché à une entité : nature et date d'expiration éventuelle. */
public record DocumentSummary(String typeDocument, String reference, LocalDate dateExpiration) {}
