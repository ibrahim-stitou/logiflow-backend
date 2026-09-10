# Orchestration du statut dossier par le module planning

Le module `planning` fait passer les **Dossier de transport** de `CREE` à `PLANIFIE` lors de la création d'un **Voyage**, et de `PLANIFIE` à `CREE` lors de l'annulation du voyage. Ces transitions ne sont pas exposées au client via `PUT /dossiers/{id}/statut` : elles passent par une extension du contrat public `DossierApi`, consommé uniquement par `VoyageService` dans la même transaction.

On a rejeté de laisser l'exploitant basculer manuellement vers `PLANIFIE` : cela produirait des dossiers « planifiés » sans voyage, incohérents avec la fiche dossier et le groupage.
