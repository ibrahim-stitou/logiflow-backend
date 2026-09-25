package com.logiflow.tms.document.infrastructure.storage;

import com.logiflow.tms.document.domain.port.out.FileStorageService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Implémentation disque local du stockage de fichiers, exposée sous {@code /fichiers/**} (voir
 * {@link StaticResourceConfig}). Amenée à être remplacée par un adaptateur S3 le jour venu.
 */
@Component
@RequiredArgsConstructor
public class LocalFileStorageAdapter implements FileStorageService {

  private static final String CHEMIN_PUBLIC = "/fichiers/";

  private final StorageProperties storageProperties;

  @Override
  public String stocker(String nomFichier, byte[] contenu, String typeContenu) {
    String nomStocke = UUID.randomUUID() + "-" + nomFichierSecurise(nomFichier);
    Path repertoire = Path.of(storageProperties.localPath());
    try {
      Files.createDirectories(repertoire);
      Files.write(repertoire.resolve(nomStocke), contenu);
    } catch (IOException e) {
      throw new IllegalStateException("Échec de stockage du fichier " + nomFichier, e);
    }
    return CHEMIN_PUBLIC + nomStocke;
  }

  @Override
  public void supprimer(String url) {
    if (url == null || !url.startsWith(CHEMIN_PUBLIC)) {
      return;
    }
    Path fichier =
        Path.of(storageProperties.localPath()).resolve(url.substring(CHEMIN_PUBLIC.length()));
    try {
      Files.deleteIfExists(fichier);
    } catch (IOException e) {
      throw new IllegalStateException("Échec de suppression du fichier " + url, e);
    }
  }

  private String nomFichierSecurise(String nomFichier) {
    if (nomFichier == null || nomFichier.isBlank()) {
      return "fichier";
    }
    return Path.of(nomFichier).getFileName().toString();
  }
}
