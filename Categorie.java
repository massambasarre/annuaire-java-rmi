package projetAnnuaire;

import java.io.Serializable;

/**
 * Énumération des trois catégories de membres de l'annuaire
 * Implémente Serializable pour la sérialisation et transmission via RMI
 */
public enum Categorie implements Serializable {
    PROFESSEUR,    // Enseignant - possède téléphone et domaine
    AUXILIAIRE,    // Personnel administratif - possède téléphone et domaine
    ETUDIANT       // Étudiant - possède matricule
}