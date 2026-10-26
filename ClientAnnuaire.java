package projetAnnuaire;

import java.rmi.Naming;
import java.util.List;
import java.util.Scanner;

/**
 * Classe ClientAnnuaire : Client public pour consulter l'annuaire
 * 
 * Fonctionnalités (sans authentification) :
 * - Lister les membres par catégorie
 * - Lister les professeurs par domaine
 * - Rechercher un membre par nom/prénom
 * 
 * Note : ce client n'a pas accès aux opérations de modification/suppression
 * (réservées au client administrateur)
 */
public class ClientAnnuaire {

    public static void main(String[] args) {
        try {
            // ===== CONNEXION AU SERVEUR RMI =====
            // Rechercher le service Annuaire enregistré sur le serveur RMI local
            IAnnuaireRemote annuaire = (IAnnuaireRemote) Naming.lookup("rmi://localhost:1099/AnnuaireService");
            System.out.println("Client connecté à l'annuaire.");

            // Création du scanner pour lire les entrées utilisateur
            Scanner sc = new Scanner(System.in);
            int choix = -1;

            // ===== BOUCLE PRINCIPALE DU MENU =====
            while (choix != 0) {
                System.out.println("\n====================================");
                System.out.println("      CLIENT ANNUAIRE INF1010");
                System.out.println("====================================");
                System.out.println("1. Lister les membres par catégorie");
                System.out.println("2. Lister les professeurs par domaine");
                System.out.println("3. Rechercher un membre");
                System.out.println("0. Quitter");
                System.out.print("→ Votre choix : ");

                // Lire et valider le choix utilisateur
                try {
                    choix = Integer.parseInt(sc.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Veuillez entrer un nombre valide.");
                    continue;
                }

                // ===== TRAITEMENT DES ACTIONS =====
                switch (choix) {
                    
                    // Cas 1 : Lister les membres par catégorie
                    case 1:
                        System.out.println("Catégorie (1=PROFESSEUR, 2=AUXILIAIRE, 3=ETUDIANT) : ");
                        String c = sc.nextLine();
                        Categorie cat = null;
                        
                        // Mapper le numéro saisi à une énumération Catégorie
                        if ("1".equals(c)) cat = Categorie.PROFESSEUR;
                        else if ("2".equals(c)) cat = Categorie.AUXILIAIRE;
                        else if ("3".equals(c)) cat = Categorie.ETUDIANT;
                        else {
                            System.out.println("Catégorie invalide.");
                            break;
                        }
                        
                        // Appeler le serveur pour récupérer les membres de cette catégorie
                        List<Personne> parCat = annuaire.listerParCategorie(cat);
                        
                        // Afficher les résultats
                        if (parCat.isEmpty()) {
                            System.out.println("Aucun membre trouvé.");
                        } else {
                            for (Personne p : parCat) {
                                System.out.println(p);
                            }
                        }
                        break;

                    // Cas 2 : Lister les professeurs par domaine
                    case 2:
                        System.out.print("Domaine : ");
                        String domaine = sc.nextLine();
                        
                        // Appeler le serveur pour récupérer les professeurs du domaine
                        List<Personne> profs = annuaire.listerProfParDomaine(domaine);
                        
                        // Afficher les résultats
                        if (profs.isEmpty()) {
                            System.out.println("Aucun professeur trouvé.");
                        } else {
                            for (Personne p : profs) {
                                System.out.println(p);
                            }
                        }
                        break;

                    // Cas 3 : Rechercher un membre par nom et prénom
                    case 3:
                        System.out.print("Nom : ");
                        String nom = sc.nextLine();
                        System.out.print("Prénom : ");
                        String prenom = sc.nextLine();
                        
                        // Appeler le serveur pour rechercher le membre
                        Personne p = annuaire.rechercherParNomPrenom(nom, prenom);
                        
                        // Afficher le résultat
                        if (p == null) {
                            System.out.println("Aucun membre trouvé.");
                        } else {
                            System.out.println(p);
                        }
                        break;

                    // Cas 0 : Quitter
                    case 0:
                        System.out.println("Au revoir.");
                        break;

                    // Cas par défaut : choix invalide
                    default:
                        System.out.println("Choix invalide.");
                }
            }

            // Fermer le scanner
            sc.close();

        } catch (Exception e) {
            // Gestion d'erreur : afficher un message si la connexion échoue
            System.err.println("Impossible de se connecter au serveur.");
            System.err.println("Détail : " + e.getMessage());
            // Le client informe l'utilisateur au lieu de planter brutalement
        }
    }
}