package com.logiflow.tms.document.api;

import java.util.UUID;

/** Événement publié avant toute modification de documents rattachés à une entité. */
public record DocumentEntiteModificationEvent(String typeEntite, UUID entiteId) {}
