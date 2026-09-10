package com.logiflow.tms.document.domain.port.out;

/**
 * Abstraction du stockage physique des fichiers de documents. L'implémentation actuelle écrit sur
 * disque local ; un remplacement futur par un stockage objet (ex. S3) n'affecte ni le domaine ni
 * l'API du module.
 */
public interface FileStorageService {

  /** Stocke le contenu et renvoie l'URL permettant de le récupérer ultérieurement. */
  String stocker(String nomFichier, byte[] contenu, String typeContenu);

  /** Supprime le fichier référencé par cette URL, silencieusement s'il n'existe plus. */
  void supprimer(String url);
}
