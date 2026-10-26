package projetAnnuaire;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe BaseDonneesAnnuaire : Gère la persistence et les opérations sur les données de l'annuaire
 * 
 * Responsabilités :
 * - Charger/sauvegarder les données de l'annuaire depuis/vers un fichier (sérialisation)
 * - Effectuer les opérations CRUD (Create, Read, Update, Delete) sur les membres
 * - Garantir la cohérence des données avec la synchronisation (synchronized)
 * - Gérer la liste rouge (blocage de membres)
 * 
 * Le stockage utilise la sérialisation Java (ObjectInputStream/ObjectOutputStream)
 * pour persister les données entre les exécutions du serveur.
 */
public class BaseDonneesAnnuaire {
    
    // Liste en mémoire contenant tous les membres de l'annuaire
    private List<Personne> membres;
    
    // Chemin du fichier de persistance (ex: "annuaire.dat")
    private String cheminFichier;

    /**
     * Constructeur : initialise la base de données
     * 
     * Logique :
     * 1. Si le fichier existe → charger les données depuis le fichier
     * 2. Si le fichier n'existe pas → créer une liste vide
     * 
     * @param cheminFichier Le chemin du fichier où persister les données
     *                      (ex: "annuaire.dat", "data/members.ser")
     */
    @SuppressWarnings("unchecked")
    public BaseDonneesAnnuaire(String cheminFichier) {
        this.cheminFichier = cheminFichier;
        
        // Créer un objet File représentant le fichier
        File f = new File(cheminFichier);
        
        // ===== CHARGEMENT DES DONNÉES =====
        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                // Lire l'objet sérialisé du fichier
                Object obj = ois.readObject();
                
                // Convertir l'objet en List<Personne> (cast)
                // @SuppressWarnings("unchecked") supprime l'avertissement du compilateur
                this.membres = (List<Personne>) obj;
                
            } catch (Exception e) {
                // En cas d'erreur lors de la desérialisation (fichier corrompu, etc.)
                e.printStackTrace();
                // Créer une liste vide comme fallback
                this.membres = new ArrayList<>();
            }
        } else {
            // Aucun fichier n'existe → créer une liste vide
            this.membres = new ArrayList<>();
        }
    }

    /**
     * Sauvegarde la liste des membres dans le fichier
     * 
     * Utilise ObjectOutputStream pour sérialiser l'objet List<Personne>
     * Cette méthode est appelée après chaque modification (ajout, suppression, modification)
     * 
     * Avantages de la sérialisation Java :
     * - Simple à utiliser
     * - Préserve la structure complète des objets
     * - Rapide pour un petit nombre de données
     */
    private void sauvegarder() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cheminFichier))) {
            // Écrire l'objet membres dans le fichier
            oos.writeObject(membres);
        } catch (IOException e) {
            // Afficher l'erreur en cas d'échec de sauvegarde
            e.printStackTrace();
        }
    }

    /**
     * Retourne la liste de tous les membres d'une catégorie donnée
     * 
     * @param cat La catégorie cherchée (PROFESSEUR, AUXILIAIRE, ou ETUDIANT)
     * @return Une liste contenant tous les membres de cette catégorie
     * 
     * Mot-clé synchronized : garantit que deux threads ne peuvent pas
     * accéder à cette méthode en même temps (évite les conditions de course)
     */
    public synchronized List<Personne> listerParCategorie(Categorie cat) {
        // Créer une nouvelle liste pour les résultats
        List<Personne> res = new ArrayList<>();
        
        // Parcourir tous les membres
        for (Personne p : membres) {
            // Ajouter à la liste si la catégorie correspond
            if (p.getCategorie() == cat) {
                res.add(p);
            }
        }
        
        // Retourner la liste filtrée
        return res;
    }

    /**
     * Retourne la liste de tous les professeurs d'un domaine spécifique
     * 
     * @param domaine Le domaine d'activité cherché (ex: "Informatique", "Mathématiques")
     * @return Une liste contenant tous les professeurs de ce domaine
     */
    public synchronized List<Personne> listerProfParDomaine(String domaine) {
        // Créer une nouvelle liste pour les résultats
        List<Personne> res = new ArrayList<>();
        
        // Parcourir tous les membres
        for (Personne p : membres) {
            // Vérifier que la personne est un PROFESSEUR
            if (p.getCategorie() == Categorie.PROFESSEUR &&
                // Vérifier que le domaine n'est pas null
                p.getDomaine() != null &&
                // Vérifier que le domaine correspond (insensible à la casse)
                p.getDomaine().equalsIgnoreCase(domaine)) {
                
                // Ajouter à la liste
                res.add(p);
            }
        }
        
        // Retourner la liste filtrée
        return res;
    }

    /**
     * Recherche un membre par son email
     * 
     * @param email L'email à rechercher
     * @return L'objet Personne trouvé, ou null si non trouvé
     * 
     * Remarque : la recherche ignore la casse (equalsIgnoreCase)
     */
    public synchronized Personne rechercherParEmail(String email) {
        // Parcourir tous les membres
        for (Personne p : membres) {
            // Vérifier que l'email n'est pas null, puis comparer (insensible à la casse)
            if (p.getEmail() != null && p.getEmail().equalsIgnoreCase(email)) {
                // Retourner immédiatement le membre trouvé
                return p;
            }
        }
        
        // Aucun membre trouvé avec cet email
        return null;
    }

    /**
     * Recherche un membre par son nom ET prénom
     * 
     * @param nom Le nom du membre
     * @param prenom Le prénom du membre
     * @return L'objet Personne trouvé, ou null si non trouvé
     * 
     * Remarque : la recherche ignore la casse pour nom et prénom
     */
    public synchronized Personne rechercherParNomPrenom(String nom, String prenom) {
        // Parcourir tous les membres
        for (Personne p : membres) {
            // Comparer nom ET prénom (insensible à la casse)
            if (p.getNom().equalsIgnoreCase(nom)
                    && p.getPrenom().equalsIgnoreCase(prenom)) {
                // Retourner immédiatement le membre trouvé
                return p;
            }
        }
        
        // Aucun membre trouvé avec ce nom/prénom
        return null;
    }

    /**
     * Ajoute un nouveau membre à l'annuaire
     * 
     * @param p L'objet Personne à ajouter
     */
    public synchronized void ajouterMembre(Personne p) {
        // Ajouter la personne à la liste en mémoire
        membres.add(p);
        
        // Sauvegarder immédiatement dans le fichier pour persister les données
        sauvegarder();
    }

    /**
     * Supprime un membre de l'annuaire
     * 
     * @param email L'email du membre à supprimer
     * @return true si le membre a été supprimé, false s'il n'existait pas
     */
    public synchronized boolean supprimerMembre(String email) {
        // Variable pour stocker le membre à supprimer
        Personne aSupprimer = null;
        
        // Rechercher le membre avec cet email
        for (Personne p : membres) {
            if (p.getEmail() != null && p.getEmail().equalsIgnoreCase(email)) {
                // Mémoriser le membre trouvé
                aSupprimer = p;
                break; // Sortir de la boucle (on a trouvé le membre)
            }
        }
        
        // ===== SUPPRESSION =====
        // Vérifier si un membre a été trouvé
        if (aSupprimer != null) {
            // Supprimer le membre de la liste
            membres.remove(aSupprimer);
            
            // Sauvegarder les changements dans le fichier
            sauvegarder();
            
            // Retourner true pour indiquer le succès
            return true;
        }
        
        // Aucun membre trouvé → suppression échouée
        return false;
    }

    /**
     * Modifie les informations d'un membre existant
     * 
     * Le membre est identifié par son email. L'objet modifie remplace l'objet ancien
     * 
     * @param modifie L'objet Personne avec les nouvelles informations
     * @return true si la modification a réussi, false si le membre n'existait pas
     */
    public synchronized boolean modifierMembre(Personne modifie) {
        // Parcourir tous les membres
        for (int i = 0; i < membres.size(); i++) {
            Personne p = membres.get(i);
            
            // Chercher le membre correspondant (par email)
            if (p.getEmail() != null 
                    && modifie.getEmail() != null 
                    && p.getEmail().equalsIgnoreCase(modifie.getEmail())) {
                
                // Remplacer l'ancien objet par le nouveau
                membres.set(i, modifie);
                
                // Sauvegarder les changements dans le fichier
                sauvegarder();
                
                // Retourner true pour indiquer le succès
                return true;
            }
        }
        
        // Aucun membre trouvé avec cet email → modification échouée
        return false;
    }

    /**
     * Ajoute un membre à la liste rouge (le "bloquer")
     * 
     * Un membre sur la liste rouge aura ses informations masquées dans les listes
     * mais sera toujours modifiable/supprimable par l'administrateur
     * 
     * @param email L'email du membre à mettre sur la liste rouge
     */
    public synchronized void mettreSurListeRouge(String email) {
        // Rechercher le membre avec cet email
        Personne p = rechercherParEmail(email);
        
        // Si le membre existe
        if (p != null) {
            // Marquer comme étant sur la liste rouge
            p.setListeRouge(true);
            
            // Sauvegarder le changement dans le fichier
            sauvegarder();
        }
        // Si le membre n'existe pas, ne rien faire (silencieusement ignoré)
    }

    /**
     * Retire un membre de la liste rouge (le "débloquer")
     * 
     * Le membre deviendra de nouveau visible dans les listes
     * 
     * @param email L'email du membre à enlever de la liste rouge
     */
    public synchronized void enleverDeListeRouge(String email) {
        // Rechercher le membre avec cet email
        Personne p = rechercherParEmail(email);
        
        // Si le membre existe
        if (p != null) {
            // Marquer comme n'étant plus sur la liste rouge
            p.setListeRouge(false);
            
            // Sauvegarder le changement dans le fichier
            sauvegarder();
        }
        // Si le membre n'existe pas, ne rien faire (silencieusement ignoré)
    }
}