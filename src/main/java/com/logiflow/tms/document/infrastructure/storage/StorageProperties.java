package com.logiflow.tms.document.infrastructure.storage;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration du stockage des fichiers de documents, préfixe {@code logiflow.storage.*}.
 *
 * <p>Aujourd'hui adossée au disque local ({@link LocalFileStorageAdapter}) ; le remplacement par un
 * stockage objet (ex. S3) se fera par un nouvel adaptateur {@code FileStorageService}, sans impact
 * sur le domaine ni sur l'API du module.
 */
@ConfigurationProperties(prefix = "logiflow.storage")
@Validated
public record StorageProperties(@NotBlank String localPath) {}
