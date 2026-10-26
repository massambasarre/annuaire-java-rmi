package projetAnnuaire;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IAnnuaireRemote extends Remote {

    // 1. Lister les membres d’une catégorie donnée
    List<Personne> listerParCategorie(Categorie categorie) throws RemoteException;

    // 2. Lister les professeurs dans un domaine d’activité donné
    List<Personne> listerProfParDomaine(String domaine) throws RemoteException;

    // 3. Rechercher un membre
    Personne rechercherParEmail(String email) throws RemoteException;
    Personne rechercherParNomPrenom(String nom, String prenom) throws RemoteException;

    // 4. Ajouter un membre
    void ajouterMembre(Personne p) throws RemoteException;

    // 5. Supprimer un membre (par email, supposé unique)
    boolean supprimerMembre(String email) throws RemoteException;

    // 6. Modifier (mettre à jour) un membre : on se base sur l'email
    boolean modifierMembre(Personne personneModifiee) throws RemoteException;

    // 7. Mettre un membre sur la liste rouge
    void mettreSurListeRouge(String email) throws RemoteException;

    // 8. Enlever un membre de la liste rouge
    void enleverDeListeRouge(String email) throws RemoteException;
}
