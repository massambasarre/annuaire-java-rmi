package projetAnnuaire;

import java.rmi.Naming;
import java.util.List;
import java.util.Scanner;

/**
 * Classe AdminClient : Client administrateur pour gérer l'annuaire via RMI
 * Permet de gérer les membres (ajouter, modifier, supprimer, rechercher, etc.)
 */
public class AdminClient {

    // Constante : mot de passe administrateur (hardcodé pour la démo)
    private static final String MOT_DE_PASSE = "montp2025";

    /**
     * Méthode principale : point d'entrée du client administrateur
     */
    public static void main(String[] args) {
        try {
            // Connexion au serveur RMI pour accéder au service Annuaire
            IAnnuaireRemote annuaire = (IAnnuaireRemote) Naming.lookup("rmi://localhost:1099/AnnuaireService");
            System.out.println("✅ Client administrateur connecté à l'annuaire.");

            // Création du scanner pour lire les entrées utilisateur
            Scanner sc = new Scanner(System.in);

            // ===== AUTHENTIFICATION ADMIN =====
            boolean adminOK = false;
            int essais = 0;
            
            // Boucle : demander le mot de passe jusqu'à 3 essais
            while (!adminOK && essais < 3) {
                System.out.print("Veuillez entrer votre mot de passe admin (3 essais max) : ");
                String mdp = sc.nextLine();
                
                // Vérifier si le mot de passe est correct
                if (MOT_DE_PASSE.equals(mdp)) {
                    adminOK = true;
                } else {
                    System.out.println("❌ Mot de passe incorrect.");
                    essais++;
                }
            }

            // Refuser l'accès après 3 tentatives échouées
            if (!adminOK) {
                System.out.println("⛔ Trop d'essais. L'accès administrateur est refusé.");
                sc.close();
                return;
            }

            // ===== MENU PRINCIPAL =====
            int choix = -1;

            // Boucle principale : afficher le menu jusqu'à ce que l'utilisateur quitte (choix = 0)
            while (choix != 0) {
                System.out.println("\n====================================");
                System.out.println("   MENU ADMINISTRATEUR ANNUAIRE");
                System.out.println("====================================");
                System.out.println("1. Lister les membres par catégorie");
                System.out.println("2. Lister les professeurs par domaine");
                System.out.println("3. Rechercher un membre (nom + prénom)");
                System.out.println("4. Ajouter un membre");
                System.out.println("5. Supprimer un membre (par email)");
                System.out.println("6. Modifier un membre");
                System.out.println("7. Mettre un membre sur la liste rouge");
                System.out.println("8. Enlever un membre de la liste rouge");
                System.out.println("0. Quitter");
                System.out.print("→ Votre choix : ");

                // Lire le choix utilisateur (avec gestion d'erreur pour les inputs non-numériques)
                try {
                    choix = Integer.parseInt(sc.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("❌ Veuillez entrer un nombre valide (0 à 8).");
                    continue;
                }

                // Vérifier que le choix est dans la plage valide
                if (choix < 0 || choix > 8) {
                    System.out.println("❌ Choix invalide. Veuillez entrer un nombre entre 0 et 8.");
                    continue;
                }

                // ===== EXÉCUTION DES ACTIONS =====
                switch (choix) {
                    case 1:
                        gererListeParCategorie(sc, annuaire);
                        break;

                    case 2:
                        gererListeProfParDomaine(sc, annuaire);
                        break;

                    case 3:
                        gererRechercheParNomPrenom(sc, annuaire);
                        break;

                    case 4:
                        ajouterMembre(sc, annuaire);
                        break;

                    case 5:
                        supprimerMembre(sc, annuaire);
                        break;

                    case 6:
                        modifierMembre(sc, annuaire);
                        break;

                    case 7:
                        mettreSurListeRouge(sc, annuaire);
                        break;

                    case 8:
                        enleverDeListeRouge(sc, annuaire);
                        break;

                    case 0:
                        System.out.println("👋 Au revoir (admin).");
                        break;
                }
            }

            // Fermer le scanner à la fin
            sc.close();

        } catch (Exception e) {
            // Capturer les erreurs de connexion au serveur
            System.err.println("❌ Impossible de se connecter au serveur.");
            System.err.println("➡ Vérifiez que le serveur est lancé puis réessayez.");
        }
    }

    // ====== MÉTHODES UTILITAIRES POUR LE MENU ======

    /**
     * Affiche tous les membres d'une catégorie donnée
     */
    private static void gererListeParCategorie(Scanner sc, IAnnuaireRemote annuaire) throws Exception {
        System.out.println("Catégorie (1=PROFESSEUR, 2=AUXILIAIRE, 3=ETUDIANT) : ");
        String c = sc.nextLine();
        Categorie cat = null;
        
        // Mapper le numéro saisi à une enum Catégorie
        if ("1".equals(c)) cat = Categorie.PROFESSEUR;
        else if ("2".equals(c)) cat = Categorie.AUXILIAIRE;
        else if ("3".equals(c)) cat = Categorie.ETUDIANT;
        else {
            System.out.println("❌ Catégorie invalide.");
            return;
        }
        
        // Récupérer les membres de cette catégorie depuis le serveur
        List<Personne> parCat = annuaire.listerParCategorie(cat);
        
        // Afficher les résultats ou un message si aucun membre trouvé
        if (parCat.isEmpty()) {
            System.out.println("ℹ Aucun membre trouvé pour cette catégorie.");
        } else {
            for (Personne p : parCat) {
                System.out.println(p);
            }
        }
    }

    /**
     * Affiche tous les professeurs d'un domaine donné
     */
    private static void gererListeProfParDomaine(Scanner sc, IAnnuaireRemote annuaire) throws Exception {
        // Demander le domaine de manière obligatoire
        String domaine = demanderChampObligatoire(sc, "Domaine (obligatoire)");
        
        // Récupérer les professeurs de ce domaine depuis le serveur
        List<Personne> profs = annuaire.listerProfParDomaine(domaine);
        
        // Afficher les résultats ou un message si aucun professeur trouvé
        if (profs.isEmpty()) {
            System.out.println("ℹ Aucun professeur trouvé pour ce domaine.");
        } else {
            for (Personne p : profs) {
                System.out.println(p);
            }
        }
    }

    /**
     * Recherche et affiche un membre par nom et prénom
     */
    private static void gererRechercheParNomPrenom(Scanner sc, IAnnuaireRemote annuaire) throws Exception {
        // Demander le nom et prénom obligatoirement
        String nom = demanderChampObligatoire(sc, "Nom (obligatoire)");
        String prenom = demanderChampObligatoire(sc, "Prénom (obligatoire)");
        
        // Rechercher le membre dans le serveur
        Personne p = annuaire.rechercherParNomPrenom(nom, prenom);
        
        // Afficher le résultat ou un message si non trouvé
        if (p == null) {
            System.out.println("ℹ Aucun membre trouvé avec ce nom/prénom.");
        } else {
            System.out.println(p);
        }
    }

    /**
     * Supprime un membre par son email
     */
    private static void supprimerMembre(Scanner sc, IAnnuaireRemote annuaire) throws Exception {
        // Demander l'email du membre à supprimer
        String emailSup = demanderEmailObligatoire(sc, "Email du membre à supprimer (obligatoire)");
        
        // Effectuer la suppression et afficher un message selon le résultat
        boolean supprime = annuaire.supprimerMembre(emailSup);
        System.out.println(supprime ? "🗑️ Membre supprimé." : "ℹ Membre introuvable.");
    }

    /**
     * Ajoute un membre à la liste rouge (blocage)
     */
    private static void mettreSurListeRouge(Scanner sc, IAnnuaireRemote annuaire) throws Exception {
        // Demander l'email du membre à mettre sur la liste rouge
        String email = demanderEmailObligatoire(sc, "Email du membre à mettre sur la liste rouge (obligatoire)");
        
        // Appeler la méthode du serveur (elle ne fait rien si le membre n'existe pas)
        annuaire.mettreSurListeRouge(email);
        System.out.println("✅ Opération terminée (si le membre existe).");
    }

    /**
     * Retire un membre de la liste rouge (déblocage)
     */
    private static void enleverDeListeRouge(Scanner sc, IAnnuaireRemote annuaire) throws Exception {
        // Demander l'email du membre à enlever de la liste rouge
        String email = demanderEmailObligatoire(sc, "Email du membre à enlever de la liste rouge (obligatoire)");
        
        // Appeler la méthode du serveur
        annuaire.enleverDeListeRouge(email);
        System.out.println("✅ Opération terminée (si le membre existe).");
    }

    // ====== AJOUT / MODIFICATION AVEC VALIDATION ======

    /**
     * Ajoute un nouveau membre à l'annuaire
     * Valide les champs obligatoires selon la catégorie
     */
    private static void ajouterMembre(Scanner sc, IAnnuaireRemote annuaire) throws Exception {
        System.out.println("\n=== AJOUT D'UN NOUVEAU MEMBRE ===");

        // Demander les informations communes à tous les membres
        String nom = demanderChampObligatoire(sc, "Nom (obligatoire)");
        String prenom = demanderChampObligatoire(sc, "Prénom (obligatoire)");

        // Demander la catégorie (PROFESSEUR, AUXILIAIRE ou ETUDIANT)
        Categorie cat = demanderCategorie(sc);

        // Le matricule n'est obligatoire que pour les étudiants
        String matricule = null;
        if (cat == Categorie.ETUDIANT) {
            matricule = demanderChampObligatoire(sc, "Matricule (obligatoire pour un étudiant)");
        }

        // L'email est obligatoire pour tous
        String email = demanderEmailObligatoire(sc, "Email (obligatoire)");

        // Le téléphone n'est obligatoire que pour les professeurs et auxiliaires
        String telephone = null;
        if (cat == Categorie.PROFESSEUR || cat == Categorie.AUXILIAIRE) {
            telephone = demanderChampObligatoire(sc, "Téléphone au bureau (obligatoire pour prof/aux)");
        }

        // Le domaine d'activité est obligatoire pour tous
        String domaine = demanderChampObligatoire(sc, "Domaine d'activité (obligatoire)");

        // Créer l'objet Personne et l'ajouter au serveur
        Personne p = new Personne(nom, prenom, cat, matricule, email, telephone, domaine, false);
        annuaire.ajouterMembre(p);
        System.out.println("✅ Membre ajouté avec succès.");
    }

    /**
     * Modifie les informations d'un membre existant
     * Permet des modifications partielles (les champs vides ne changent pas)
     */
    private static void modifierMembre(Scanner sc, IAnnuaireRemote annuaire) throws Exception {
        System.out.println("\n=== MODIFICATION D'UN MEMBRE ===");
        
        // Demander l'email du membre à modifier
        String email = demanderEmailObligatoire(sc, "Email du membre à modifier (obligatoire)");

        // Rechercher le membre existant
        Personne existant = annuaire.rechercherParEmail(email);
        if (existant == null) {
            System.out.println("ℹ Membre introuvable.");
            return;
        }

        // Afficher les informations actuelles
        System.out.println("Membre actuel : " + existant);

        // Demander les nouveaux champs (optionnel : laisser vide pour ne pas changer)
        System.out.print("Nouveau nom (laisser vide pour ne pas changer) : ");
        String nom = sc.nextLine().trim();
        if (!nom.isEmpty()) existant.setNom(nom);

        System.out.print("Nouveau prénom (laisser vide pour ne pas changer) : ");
        String prenom = sc.nextLine().trim();
        if (!prenom.isEmpty()) existant.setPrenom(prenom);

        System.out.print("Nouveau domaine (laisser vide pour ne pas changer) : ");
        String domaine = sc.nextLine().trim();
        if (!domaine.isEmpty()) existant.setDomaine(domaine);

        System.out.print("Nouveau téléphone (laisser vide pour ne pas changer) : ");
        String tel = sc.nextLine().trim();
        if (!tel.isEmpty()) existant.setTelephone(tel);

        // Le matricule ne peut être modifié que pour les étudiants
        if (existant.getCategorie() == Categorie.ETUDIANT) {
            System.out.print("Nouveau matricule (laisser vide pour ne pas changer) : ");
            String mat = sc.nextLine().trim();
            if (!mat.isEmpty()) existant.setMatricule(mat);
        }

        // Envoyer la modification au serveur
        boolean ok = annuaire.modifierMembre(existant);
        System.out.println(ok ? "✅ Membre modifié." : "❌ Erreur lors de la modification.");
    }

    // ====== FONCTIONS DE VALIDATION ======

    /**
     * Demande à l'utilisateur de remplir un champ obligatoire
     * Boucle tant que le champ reste vide
     */
    private static String demanderChampObligatoire(Scanner sc, String label) {
        while (true) {
            System.out.print(label + " : ");
            String val = sc.nextLine().trim();
            
            // Vérifier que le champ n'est pas vide
            if (!val.isEmpty()) {
                return val;
            }
            System.out.println("❌ Ce champ est OBLIGATOIRE. Merci de le remplir.");
        }
    }

    /**
     * Demande à l'utilisateur d'entrer un email valide
     * Valide la présence d'un @ et rejeite les champs vides
     */
    private static String demanderEmailObligatoire(Scanner sc, String label) {
        while (true) {
            System.out.print(label + " : ");
            String email = sc.nextLine().trim();
            
            // Vérifier que le champ n'est pas vide
            if (email.isEmpty()) {
                System.out.println("❌ L'email est OBLIGATOIRE.");
                continue;
            }
            
            // Vérifier que l'email contient un @
            if (!email.contains("@")) {
                System.out.println("❌ Email invalide. Exemple : user@domaine.com");
                continue;
            }
            
            return email;
        }
    }

    /**
     * Demande à l'utilisateur de choisir une catégorie
     * Boucle tant que le choix est invalide
     */
    private static Categorie demanderCategorie(Scanner sc) {
        while (true) {
            System.out.println("Catégorie (obligatoire) : ");
            System.out.println("  1 = PROFESSEUR");
            System.out.println("  2 = AUXILIAIRE");
            System.out.println("  3 = ETUDIANT");
            System.out.print("→ Votre choix : ");
            String c = sc.nextLine().trim();
            
            // Mapper le choix numérique à une enum Catégorie
            switch (c) {
                case "1":
                    return Categorie.PROFESSEUR;
                case "2":
                    return Categorie.AUXILIAIRE;
                case "3":
                    return Categorie.ETUDIANT;
                default:
                    System.out.println("❌ Choix invalide. Veuillez entrer 1, 2 ou 3.");
            }
        }
    }
}