package projetAnnuaire;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe AnnuaireImpl : Implémentation du service RMI pour gérer l'annuaire
 * 
 * Cette classe :
 * - Étend UnicastRemoteObject pour être accessible via RMI (Remote Method Invocation)
 * - Implémente l'interface IAnnuaireRemote définissant les méthodes distantes
 * - Delègue les opérations à BaseDonneesAnnuaire
 * - Applique des règles de sécurité (liste rouge) sur les données retournées
 */
public class AnnuaireImpl extends UnicastRemoteObject implements IAnnuaireRemote {
    
    // serialVersionUID : identifiant unique pour la sérialisation des objets RMI
    private static final long serialVersionUID = 1L;
    
    // Référence à la base de données de l'annuaire
    private BaseDonneesAnnuaire base;

    /**
     * Constructeur : initialise le service RMI avec une base de données
     * 
     * @param base La base de données contenant les informations de l'annuaire
     * @throws RemoteException En cas d'erreur lors de l'initialisation RMI
     */
    protected AnnuaireImpl(BaseDonneesAnnuaire base) throws RemoteException {
        super(); // Appel du constructeur de UnicastRemoteObject pour activer RMI
        this.base = base;
    }

    /**
     * Retourne la liste de tous les membres d'une catégorie donnée
     * Applique la règle de liste rouge pour masquer les infos des membres bloqués
     * 
     * @param categorie La catégorie cherchée (PROFESSEUR, AUXILIAIRE, ETUDIANT)
     * @return Liste des membres, avec données masquées pour ceux en liste rouge
     * @throws RemoteException En cas d'erreur RMI
     */
    @Override
    public java.util.List<Personne> listerParCategorie(Categorie categorie) throws RemoteException {
        // Récupérer les données brutes de la base
        List<Personne> source = base.listerParCategorie(categorie);
        
        // Appliquer les règles de sécurité (masquer les infos de liste rouge)
        return appliquerRegleListeRouge(source);
    }

    /**
     * Retourne la liste de tous les professeurs d'un domaine donné
     * Applique la règle de liste rouge pour masquer les infos des membres bloqués
     * 
     * @param domaine Le domaine d'activité recherché
     * @return Liste des professeurs, avec données masquées pour ceux en liste rouge
     * @throws RemoteException En cas d'erreur RMI
     */
    @Override
    public java.util.List<Personne> listerProfParDomaine(String domaine) throws RemoteException {
        // Récupérer les données brutes de la base
        List<Personne> source = base.listerProfParDomaine(domaine);
        
        // Appliquer les règles de sécurité
        return appliquerRegleListeRouge(source);
    }

    /**
     * Recherche un membre par son email (sans appliquer la règle de liste rouge)
     * Utilisé principalement par l'admin pour modifier/supprimer un membre
     * 
     * @param email L'email du membre à rechercher
     * @return L'objet Personne trouvé, ou null si non trouvé
     * @throws RemoteException En cas d'erreur RMI
     */
    @Override
    public Personne rechercherParEmail(String email) throws RemoteException {
        // La base retourne directement les données, sans filtrage
        // Cela permet à l'admin d'accéder aux infos même si la personne est en liste rouge
        return base.rechercherParEmail(email);
    }

    /**
     * Recherche un membre par son nom et prénom
     * Applique la règle de liste rouge pour masquer les infos si nécessaire
     * 
     * @param nom Le nom du membre
     * @param prenom Le prénom du membre
     * @return L'objet Personne trouvé avec infos masquées si en liste rouge, ou null
     * @throws RemoteException En cas d'erreur RMI
     */
    @Override
    public Personne rechercherParNomPrenom(String nom, String prenom) throws RemoteException {
        // Récupérer le membre de la base
        Personne p = base.rechercherParNomPrenom(nom, prenom);
        
        // Si le membre n'existe pas, retourner null
        if (p == null) return null;
        
        // Si le membre est sur la liste rouge, retourner une copie masquée
        if (p.isListeRouge()) {
            return p.copieListeRouge();
        }
        
        // Sinon, retourner les données complètes
        return p;
    }

    /**
     * Ajoute un nouveau membre à l'annuaire
     * 
     * @param p Le membre à ajouter
     * @throws RemoteException En cas d'erreur RMI
     */
    @Override
    public void ajouterMembre(Personne p) throws RemoteException {
        // Déléguer l'ajout à la base de données
        base.ajouterMembre(p);
    }

    /**
     * Supprime un membre de l'annuaire
     * 
     * @param email L'email du membre à supprimer
     * @return true si le membre a été supprimé, false s'il n'existait pas
     * @throws RemoteException En cas d'erreur RMI
     */
    @Override
    public boolean supprimerMembre(String email) throws RemoteException {
        // Déléguer la suppression à la base de données
        return base.supprimerMembre(email);
    }

    /**
     * Modifie les informations d'un membre existant
     * 
     * @param personneModifiee L'objet Personne avec les nouvelles informations
     * @return true si la modification a réussi, false sinon
     * @throws RemoteException En cas d'erreur RMI
     */
    @Override
    public boolean modifierMembre(Personne personneModifiee) throws RemoteException {
        // Déléguer la modification à la base de données
        return base.modifierMembre(personneModifiee);
    }

    /**
     * Ajoute un membre à la liste rouge (le "bloquer")
     * Les informations du membre bloqué seront masquées dans les listes
     * 
     * @param email L'email du membre à mettre sur la liste rouge
     * @throws RemoteException En cas d'erreur RMI
     */
    @Override
    public void mettreSurListeRouge(String email) throws RemoteException {
        // Déléguer l'ajout à la liste rouge à la base de données
        base.mettreSurListeRouge(email);
    }

    /**
     * Retire un membre de la liste rouge (le "débloquer")
     * Les informations du membre seront de nouveau visibles dans les listes
     * 
     * @param email L'email du membre à enlever de la liste rouge
     * @throws RemoteException En cas d'erreur RMI
     */
    @Override
    public void enleverDeListeRouge(String email) throws RemoteException {
        // Déléguer le retrait de la liste rouge à la base de données
        base.enleverDeListeRouge(email);
    }

    // ====== MÉTHODE UTILITAIRE DE SÉCURITÉ ======

    /**
     * Applique la règle de liste rouge à une liste de membres
     * 
     * Cette méthode masque les informations des membres bloqués :
     * - Si un membre est en liste rouge, retourner une copie avec données masquées
     * - Si un membre n'est pas en liste rouge, retourner les données complètes
     * 
     * Cela garantit que les données sensibles ne sont jamais exposées publiquement
     * pour les membres bloqués, même s'ils apparaissent dans une liste.
     * 
     * @param source La liste de membres brute (sans filtrage)
     * @return Une nouvelle liste avec les informations masquées pour la liste rouge
     */
    private List<Personne> appliquerRegleListeRouge(List<Personne> source) {
        // Créer une nouvelle liste pour le résultat
        List<Personne> resultat = new ArrayList<>();
        
        // Pour chaque personne dans la source
        for (Personne p : source) {
            // Si la personne est sur la liste rouge
            if (p.isListeRouge()) {
                // Ajouter une copie masquée (avec infos sensibles supprimées)
                resultat.add(p.copieListeRouge());
            } else {
                // Sinon, ajouter la personne avec toutes ses infos
                resultat.add(p);
            }
        }
        
        // Retourner la liste avec les données masquées
        return resultat;
    }
}