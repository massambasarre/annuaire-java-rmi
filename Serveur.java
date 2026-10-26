package projetAnnuaire;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 * Classe Serveur : Lance le serveur RMI de l'annuaire
 * Crée le registre RMI, initialise la base, et enregistre le service
 */
public class Serveur {

    public static void main(String[] args) {
        try {
            // Démarrer le registre RMI sur le port 1099
            try {
                LocateRegistry.createRegistry(1099);
                System.out.println("Registre RMI démarré sur le port 1099.");
            } catch (Exception e) {
                // Le registre existe déjà
                System.out.println("⚠ Le registre RMI semble déjà lancé (BIENVENUE!).");
            }

            // Charger la base de données depuis le fichier
            BaseDonneesAnnuaire base = new BaseDonneesAnnuaire("annuaire.dat");

            // Créer l'implémentation du service
            IAnnuaireRemote annuaire = new AnnuaireImpl(base);

            // Enregistrer le service dans le registre RMI
            // Les clients pourront le trouver via : rmi://localhost:1099/AnnuaireService
            Naming.rebind("rmi://localhost:1099/AnnuaireService", annuaire);

            System.out.println("✅ Serveur de l'annuaire INF1010 démarré avec succès !");

        } catch (Exception e) {
            System.err.println("Erreur serveur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}