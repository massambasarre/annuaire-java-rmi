package projetAnnuaire;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.List;

public class AnnuaireGUI extends JFrame {

    // Bleu principal
    private static final Color THEME_BLUE = new Color(30, 90, 200);

    // Bleu très clair pour le fond
    private static final Color THEME_LIGHT = new Color(220, 230, 255);

    private static final String ADMIN_PASSWORD = System.getenv("ANNUAIRE_ADMIN_PASSWORD");
    private static final int ADMIN_MAX_ESSAIS = 3;

    private IAnnuaireRemote annuaire;

    // Layout principal : cartes (Accueil / Client / Admin)
    private CardLayout cardLayout;
    private JPanel panelCartes;

    // Zone d'affichage des résultats
    private JTextArea txtResultats;

    // Champs consultation (client)
    private JComboBox<Categorie> comboCategorie;
    private JTextField txtDomaineProf;
    private JTextField txtNomRecherche;
    private JTextField txtPrenomRecherche;

    // Champs admin
    private JPasswordField txtMdpAdmin;
    private JLabel lblEtatAdmin;
    private boolean adminConnecte = false;
    private int adminEssais = 0;
    private JButton btnConnexionAdmin;

    private JTextField txtNomAdmin;
    private JTextField txtPrenomAdmin;
    private JComboBox<Categorie> comboCategorieAdmin;
    private JTextField txtMatriculeAdmin;
    private JTextField txtEmailAdmin;
    private JTextField txtTelAdmin;
    private JTextField txtDomaineAdmin;
    private JTextField txtEmailCible;
    private JCheckBox chkListeRouge;

    public AnnuaireGUI() {
        setTitle("Annuaire RMI - Interface graphique");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        getContentPane().setBackground(THEME_LIGHT);

        // Connexion au serveur RMI
        try {
            annuaire = (IAnnuaireRemote) Naming.lookup("rmi://localhost:1099/AnnuaireService");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Impossible de se connecter au serveur.\nVérifiez que le serveur Annuaire est démarré.",
                    "Erreur de connexion",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        initUI();
    }

    private void initUI() {
        // Cartes : accueil, client, admin
        cardLayout = new CardLayout();
        panelCartes = new JPanel(cardLayout);

        panelCartes.add(creerPanelAccueil(), "accueil");
        panelCartes.add(creerPanelConsultation(), "client");
        panelCartes.add(creerPanelAdministration(), "admin");

        // Zone résultat partagée
        txtResultats = new JTextArea();
        txtResultats.setEditable(false);
        txtResultats.setBackground(new Color(200, 220, 255)); // Bleu clair
        txtResultats.setForeground(Color.BLACK);              // Texte foncé lisible
        txtResultats.setFont(txtResultats.getFont().deriveFont(14f));
        txtResultats.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); // marges


        JScrollPane scroll = new JScrollPane(txtResultats);
        scroll.getViewport().setBackground(THEME_LIGHT);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panelCartes, BorderLayout.NORTH);
        getContentPane().add(scroll, BorderLayout.CENTER);

        // Message d'accueil la toute première fois
        afficherMessageAccueil();

        // Afficher d'abord la carte accueil
        cardLayout.show(panelCartes, "accueil");
    }

    // ============ PAGE D'ACCUEIL ============

    private JPanel creerPanelAccueil() {
        // On essaie de charger une image de fond (background.jpg dans src/projetAnnuaire)
        Image bgImage = null;
        try {
            java.net.URL url = getClass().getResource("/projetAnnuaire/background.jpg");
            if (url != null) {
                bgImage = new ImageIcon(url).getImage();
            }
        } catch (Exception e) {
            // si erreur, on garde un fond bleu clair simple
        }

        BackgroundPanel panel = new BackgroundPanel(bgImage);
        panel.setLayout(new BorderLayout());

        // Titre en haut
        JLabel lblTitre = new JLabel("Annuaire RMI INF1010", SwingConstants.CENTER);
        lblTitre.setForeground(THEME_BLUE);
        lblTitre.setFont(lblTitre.getFont().deriveFont(Font.BOLD, 28f));
        lblTitre.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        panel.add(lblTitre, BorderLayout.NORTH);

        // Colonne à gauche : texte + boutons
        JPanel gauche = new JPanel();
        gauche.setOpaque(false);
        gauche.setLayout(new BoxLayout(gauche, BoxLayout.Y_AXIS));
        gauche.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel lblIntro = new JLabel(
                "<html>" +
                "<b>Bienvenue dans l’application d’annuaire distribuée.</b><br><br>" +
                "Veuillez choisir un type d’accès :" +
                "</html>"
        );
        lblIntro.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnClient = new JButton("Accès CLIENT (Consultation)");
        btnClient.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnClient.setForeground(THEME_BLUE);
        btnClient.setFont(btnClient.getFont().deriveFont(Font.BOLD, 14f));
        btnClient.addActionListener(e -> {
            clearResultats(); // on repart propre
            cardLayout.show(panelCartes, "client");
        });

        JButton btnAdmin = new JButton("Accès ADMINISTRATEUR");
        btnAdmin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAdmin.setForeground(THEME_BLUE);
        btnAdmin.setFont(btnAdmin.getFont().deriveFont(Font.BOLD, 14f));
        btnAdmin.addActionListener(e -> {
            clearResultats(); // on repart propre
            cardLayout.show(panelCartes, "admin");
        });

        gauche.add(lblIntro);
        gauche.add(Box.createVerticalStrut(20));
        gauche.add(btnClient);
        gauche.add(Box.createVerticalStrut(10));
        gauche.add(btnAdmin);

        panel.add(gauche, BorderLayout.WEST);

        return panel;
    }

    // ============ CARTE CLIENT / CONSULTATION ============

    private JPanel creerPanelConsultation() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(THEME_LIGHT);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // 🔙 Bouton retour vers l'accueil
        JButton btnRetour = new JButton("⬅ Retour à l'accueil");
        btnRetour.setForeground(THEME_BLUE);
        btnRetour.addActionListener(e -> {
            clearResultats();          // on vide la zone du bas
            afficherMessageAccueil();  // on remet le message d'accueil
            cardLayout.show(panelCartes, "accueil");
        });
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 3;
        panel.add(btnRetour, c);
        c.gridwidth = 1;

        row++;

        // Lister par catégorie
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel("Lister par catégorie :"), c);

        comboCategorie = new JComboBox<>(Categorie.values());
        c.gridx = 1;
        panel.add(comboCategorie, c);

        JButton btnListerCat = new JButton("Lister");
        btnListerCat.setForeground(THEME_BLUE);
        btnListerCat.addActionListener(this::onListerParCategorie);
        c.gridx = 2;
        panel.add(btnListerCat, c);

        row++;

        // Lister profs par domaine
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel("Profs par domaine * :"), c);

        txtDomaineProf = new JTextField(15);
        c.gridx = 1;
        panel.add(txtDomaineProf, c);

        JButton btnListerProfs = new JButton("Lister profs");
        btnListerProfs.setForeground(THEME_BLUE);
        btnListerProfs.addActionListener(this::onListerProfsParDomaine);
        c.gridx = 2;
        panel.add(btnListerProfs, c);

        row++;

        // Rechercher par nom/prénom
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel("Nom * :"), c);
        txtNomRecherche = new JTextField(10);
        c.gridx = 1;
        panel.add(txtNomRecherche, c);

        row++;
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel("Prénom * :"), c);
        txtPrenomRecherche = new JTextField(10);
        c.gridx = 1;
        panel.add(txtPrenomRecherche, c);

        JButton btnRechercheNP = new JButton("Rechercher");
        btnRechercheNP.setForeground(THEME_BLUE);
        btnRechercheNP.addActionListener(this::onRechercherNomPrenom);
        c.gridx = 2;
        panel.add(btnRechercheNP, c);

        return panel;
    }

    // Actions client

    private void onListerParCategorie(ActionEvent e) {
        clearResultats();
        Categorie cat = (Categorie) comboCategorie.getSelectedItem();
        try {
            List<Personne> liste = annuaire.listerParCategorie(cat);
            if (liste.isEmpty()) {
                appendResultat("Aucun membre trouvé pour la catégorie " + cat + "\n");
            } else {
                appendResultat("Membres de la catégorie " + cat + " :\n");
                for (Personne p : liste) {
                    appendResultat(" - " + p + "\n");
                }
            }
        } catch (RemoteException ex) {
            afficherErreurRMI(ex);
        }
    }

    private void onListerProfsParDomaine(ActionEvent e) {
        clearResultats();
        String domaine = txtDomaineProf.getText().trim();
        if (domaine.isEmpty()) {
            appendResultat("❌ Domaine obligatoire.\n");
            return;
        }
        try {
            List<Personne> liste = annuaire.listerProfParDomaine(domaine);
            if (liste.isEmpty()) {
                appendResultat("Aucun professeur trouvé pour le domaine \"" + domaine + "\".\n");
            } else {
                appendResultat("Professeurs du domaine \"" + domaine + "\" :\n");
                for (Personne p : liste) {
                    appendResultat(" - " + p + "\n");
                }
            }
        } catch (RemoteException ex) {
            afficherErreurRMI(ex);
        }
    }

    private void onRechercherNomPrenom(ActionEvent e) {
        clearResultats();
        String nom = txtNomRecherche.getText().trim();
        String prenom = txtPrenomRecherche.getText().trim();
        if (nom.isEmpty() || prenom.isEmpty()) {
            appendResultat("❌ Nom et prénom sont obligatoires.\n");
            return;
        }
        try {
            Personne p = annuaire.rechercherParNomPrenom(nom, prenom);
            if (p == null) {
                appendResultat("Aucun membre trouvé pour " + nom + " " + prenom + ".\n");
            } else {
                appendResultat("Membre trouvé : " + p + "\n");
            }
        } catch (RemoteException ex) {
            afficherErreurRMI(ex);
        }
    }

    // ============ CARTE ADMINISTRATION ============

    private JPanel creerPanelAdministration() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(THEME_LIGHT);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // 🔙 Bouton retour vers l'accueil
        JButton btnRetour = new JButton("⬅ Retour à l'accueil");
        btnRetour.setForeground(THEME_BLUE);
        btnRetour.addActionListener(e -> {
            clearResultats();          // on vide la zone du bas
            afficherMessageAccueil();  // on remet le message d'accueil
            cardLayout.show(panelCartes, "accueil");
        });
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 3;
        panel.add(btnRetour, c);
        c.gridwidth = 1;

        row++;

        // Connexion admin
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel("Mot de passe admin :"), c);
        txtMdpAdmin = new JPasswordField(10);
        c.gridx = 1;
        panel.add(txtMdpAdmin, c);

        btnConnexionAdmin = new JButton("Connexion");
        btnConnexionAdmin.setForeground(THEME_BLUE);
        btnConnexionAdmin.addActionListener(this::onConnexionAdmin);
        c.gridx = 2;
        panel.add(btnConnexionAdmin, c);

        row++;
        lblEtatAdmin = new JLabel("Non connecté");
        lblEtatAdmin.setForeground(Color.RED);
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 3;
        panel.add(lblEtatAdmin, c);
        c.gridwidth = 1;

        row++;

        // Formulaire membre
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel("Nom * :"), c);
        txtNomAdmin = new JTextField(10);
        c.gridx = 1;
        c.gridwidth = 2;
        panel.add(txtNomAdmin, c);
        c.gridwidth = 1;

        row++;
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel("Prénom * :"), c);
        txtPrenomAdmin = new JTextField(10);
        c.gridx = 1;
        c.gridwidth = 2;
        panel.add(txtPrenomAdmin, c);
        c.gridwidth = 1;

        row++;
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel("Catégorie * :"), c);
        comboCategorieAdmin = new JComboBox<>(Categorie.values());
        c.gridx = 1;
        c.gridwidth = 2;
        panel.add(comboCategorieAdmin, c);
        c.gridwidth = 1;

        row++;
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel("Matricule * (si étudiant) :"), c);
        txtMatriculeAdmin = new JTextField(10);
        c.gridx = 1;
        c.gridwidth = 2;
        panel.add(txtMatriculeAdmin, c);
        c.gridwidth = 1;

        row++;
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel("Email * (référence) :"), c);
        txtEmailAdmin = new JTextField(15);
        c.gridx = 1;
        c.gridwidth = 2;
        panel.add(txtEmailAdmin, c);
        c.gridwidth = 1;

        row++;
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel("Téléphone (prof/aux) :"), c);
        txtTelAdmin = new JTextField(10);
        c.gridx = 1;
        c.gridwidth = 2;
        panel.add(txtTelAdmin, c);
        c.gridwidth = 1;

        row++;
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel("Domaine d'activité * :"), c);
        txtDomaineAdmin = new JTextField(15);
        c.gridx = 1;
        c.gridwidth = 2;
        panel.add(txtDomaineAdmin, c);
        c.gridwidth = 1;

        row++;
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel("Sur liste rouge ?"), c);
        chkListeRouge = new JCheckBox();
        chkListeRouge.setBackground(THEME_LIGHT);
        c.gridx = 1;
        panel.add(chkListeRouge, c);

        row++;

        // Email cible pour suppression / liste rouge
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel("Email cible (suppr / liste rouge) :"), c);
        txtEmailCible = new JTextField(15);
        c.gridx = 1;
        c.gridwidth = 2;
        panel.add(txtEmailCible, c);
        c.gridwidth = 1;

        row++;

        // Boutons d'action
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBoutons.setBackground(THEME_LIGHT);

        JButton btnAjouter = new JButton("Ajouter membre");
        btnAjouter.setForeground(THEME_BLUE);
        btnAjouter.addActionListener(this::onAjouterMembre);
        panelBoutons.add(btnAjouter);

        JButton btnModifier = new JButton("Modifier membre");
        btnModifier.setForeground(THEME_BLUE);
        btnModifier.addActionListener(this::onModifierMembre);
        panelBoutons.add(btnModifier);

        JButton btnSupprimer = new JButton("Supprimer membre");
        btnSupprimer.setForeground(THEME_BLUE);
        btnSupprimer.addActionListener(this::onSupprimerMembre);
        panelBoutons.add(btnSupprimer);

        JButton btnListeRougeOn = new JButton("Mettre sur liste rouge");
        btnListeRougeOn.setForeground(THEME_BLUE);
        btnListeRougeOn.addActionListener(this::onMettreListeRouge);
        panelBoutons.add(btnListeRougeOn);

        JButton btnListeRougeOff = new JButton("Retirer de la liste rouge");
        btnListeRougeOff.setForeground(THEME_BLUE);
        btnListeRougeOff.addActionListener(this::onEnleverListeRouge);
        panelBoutons.add(btnListeRougeOff);

        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 3;
        panel.add(panelBoutons, c);
        c.gridwidth = 1;

        // Champs admin désactivés au début
        setAdminEnabled(false);

        return panel;
    }

    private void onConnexionAdmin(ActionEvent e) {
        if (adminEssais >= ADMIN_MAX_ESSAIS) {
            appendResultat("⛔ Accès administrateur bloqué (trop de tentatives).\n");
            return;
        }

        String mdp = new String(txtMdpAdmin.getPassword());

        if (ADMIN_PASSWORD != null && !ADMIN_PASSWORD.isEmpty() && ADMIN_PASSWORD.equals(mdp)) {
            adminConnecte = true;
            lblEtatAdmin.setText("Connecté en tant qu'administrateur");
            lblEtatAdmin.setForeground(THEME_BLUE);
            setAdminEnabled(true);
            appendResultat("✅ Connexion administrateur réussie.\n");
        } else {
            adminEssais++;
            adminConnecte = false;
            lblEtatAdmin.setText("Mot de passe incorrect (" + adminEssais + "/" + ADMIN_MAX_ESSAIS + ")");
            lblEtatAdmin.setForeground(Color.RED);
            setAdminEnabled(false);
            appendResultat("❌ Mot de passe admin incorrect.\n");

            if (adminEssais >= ADMIN_MAX_ESSAIS) {
                lblEtatAdmin.setText("Accès admin bloqué après " + ADMIN_MAX_ESSAIS + " tentatives.");
                txtMdpAdmin.setEnabled(false);
                btnConnexionAdmin.setEnabled(false);

                appendResultat("⛔ Vous avez dépassé le nombre maximum de tentatives.\n\n");
                appendResultat("➡ Veuillez redémarrer l'application pour réessayer.\n");
                appendResultat("📧 Pour assistance : admin@annuaire.local\n");
                appendResultat("☎ Support : 514-000-0000\n");
            }
        }
    }

    private void setAdminEnabled(boolean enabled) {
        if (txtNomAdmin != null) txtNomAdmin.setEnabled(enabled);
        if (txtPrenomAdmin != null) txtPrenomAdmin.setEnabled(enabled);
        if (comboCategorieAdmin != null) comboCategorieAdmin.setEnabled(enabled);
        if (txtMatriculeAdmin != null) txtMatriculeAdmin.setEnabled(enabled);
        if (txtEmailAdmin != null) txtEmailAdmin.setEnabled(enabled);
        if (txtTelAdmin != null) txtTelAdmin.setEnabled(enabled);
        if (txtDomaineAdmin != null) txtDomaineAdmin.setEnabled(enabled);
        if (chkListeRouge != null) chkListeRouge.setEnabled(enabled);
        if (txtEmailCible != null) txtEmailCible.setEnabled(enabled);
    }

    private void onAjouterMembre(ActionEvent e) {
        clearResultats();
        if (!adminConnecte) {
            appendResultat("❌ Vous devez être connecté en admin.\n");
            return;
        }

        String nom = txtNomAdmin.getText().trim();
        String prenom = txtPrenomAdmin.getText().trim();
        Categorie cat = (Categorie) comboCategorieAdmin.getSelectedItem();
        String matricule = txtMatriculeAdmin.getText().trim();
        String email = txtEmailAdmin.getText().trim();
        String tel = txtTelAdmin.getText().trim();
        String domaine = txtDomaineAdmin.getText().trim();
        boolean listeRouge = chkListeRouge.isSelected();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || !email.contains("@") || domaine.isEmpty()) {
            appendResultat("❌ Nom, prénom, email (valide) et domaine sont obligatoires.\n");
            return;
        }
        if (cat == Categorie.ETUDIANT && matricule.isEmpty()) {
            appendResultat("❌ Matricule obligatoire pour un étudiant.\n");
            return;
        }
        if ((cat == Categorie.PROFESSEUR || cat == Categorie.AUXILIAIRE) && tel.isEmpty()) {
            appendResultat("❌ Téléphone obligatoire pour un professeur/auxiliaire.\n");
            return;
        }

        Personne p = new Personne(
                nom,
                prenom,
                cat,
                matricule.isEmpty() ? null : matricule,
                email,
                tel.isEmpty() ? null : tel,
                domaine,
                listeRouge
        );

        try {
            annuaire.ajouterMembre(p);
            appendResultat("✅ Membre ajouté : " + p + "\n");
        } catch (RemoteException ex) {
            afficherErreurRMI(ex);
        }
    }

    /**
     * Modification d'un membre existant.
     * On se base sur l'email saisi dans le formulaire (email = identifiant).
     * Côté serveur, IAnnuaireRemote.modifierMembre(Personne) utilise cet email
     * pour retrouver et mettre à jour le membre.
     */
    private void onModifierMembre(ActionEvent e) {
        clearResultats();
        if (!adminConnecte) {
            appendResultat("❌ Vous devez être connecté en admin pour modifier un membre.\n");
            return;
        }

        String nom = txtNomAdmin.getText().trim();
        String prenom = txtPrenomAdmin.getText().trim();
        Categorie cat = (Categorie) comboCategorieAdmin.getSelectedItem();
        String matricule = txtMatriculeAdmin.getText().trim();
        String email = txtEmailAdmin.getText().trim();
        String tel = txtTelAdmin.getText().trim();
        String domaine = txtDomaineAdmin.getText().trim();
        boolean listeRouge = chkListeRouge.isSelected();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || !email.contains("@") || domaine.isEmpty()) {
            appendResultat("❌ Pour modifier, nom, prénom, email (valide) et domaine sont obligatoires.\n");
            return;
        }
        if (cat == Categorie.ETUDIANT && matricule.isEmpty()) {
            appendResultat("❌ Matricule obligatoire pour un étudiant.\n");
            return;
        }
        if ((cat == Categorie.PROFESSEUR || cat == Categorie.AUXILIAIRE) && tel.isEmpty()) {
            appendResultat("❌ Téléphone obligatoire pour un professeur/auxiliaire.\n");
            return;
        }

        Personne pModifiee = new Personne(
                nom,
                prenom,
                cat,
                matricule.isEmpty() ? null : matricule,
                email,
                tel.isEmpty() ? null : tel,
                domaine,
                listeRouge
        );

        try {
            boolean ok = annuaire.modifierMembre(pModifiee);
            if (ok) {
                appendResultat("✅ Membre modifié avec succès : " + pModifiee + "\n");
            } else {
                appendResultat("ℹ Aucun membre trouvé avec l'email (référence) : " + email + "\n");
            }
        } catch (RemoteException ex) {
            afficherErreurRMI(ex);
        }
    }

    private void onSupprimerMembre(ActionEvent e) {
        clearResultats();
        if (!adminConnecte) {
            appendResultat("❌ Vous devez être connecté en admin.\n");
            return;
        }
        String email = txtEmailCible.getText().trim();
        if (email.isEmpty()) {
            appendResultat("❌ Email cible obligatoire.\n");
            return;
        }
        try {
            boolean ok = annuaire.supprimerMembre(email);
            if (ok) {
                appendResultat("🗑 Membre supprimé : " + email + "\n");
            } else {
                appendResultat("ℹ Aucun membre trouvé avec cet email : " + email + "\n");
            }
        } catch (RemoteException ex) {
            afficherErreurRMI(ex);
        }
    }

    private void onMettreListeRouge(ActionEvent e) {
        clearResultats();
        if (!adminConnecte) {
            appendResultat("❌ Vous devez être connecté en admin.\n");
            return;
        }
        String email = txtEmailCible.getText().trim();
        if (email.isEmpty()) {
            appendResultat("❌ Email cible obligatoire.\n");
            return;
        }
        try {
            annuaire.mettreSurListeRouge(email);
            appendResultat("✅ Membre mis sur liste rouge : " + email + "\n");
        } catch (RemoteException ex) {
            afficherErreurRMI(ex);
        }
    }

    private void onEnleverListeRouge(ActionEvent e) {
        clearResultats();
        if (!adminConnecte) {
            appendResultat("❌ Vous devez être connecté en admin.\n");
            return;
        }
        String email = txtEmailCible.getText().trim();
        if (email.isEmpty()) {
            appendResultat("❌ Email cible obligatoire.\n");
            return;
        }
        try {
            annuaire.enleverDeListeRouge(email);
            appendResultat("✅ Membre retiré de la liste rouge : " + email + "\n");
        } catch (RemoteException ex) {
            afficherErreurRMI(ex);
        }
    }

    // ============ UTILITAIRES ============

    private void appendResultat(String txt) {
        txtResultats.append(txt);
    }

    private void clearResultats() {
        txtResultats.setText("");
    }

    private void afficherMessageAccueil() {
        appendResultat("Bienvenue dans l’annuaire RMI INF1010.\n");
        appendResultat("Utilisez la page d'accueil pour choisir l'accès CLIENT ou ADMIN.\n\n");
    }

    private void afficherErreurRMI(Exception ex) {
        appendResultat("❌ Erreur de communication avec le serveur.\n");
        // ex.printStackTrace(); // si tu veux debug
    }

    // ======== PANEL AVEC IMAGE DE FOND ========
    private static class BackgroundPanel extends JPanel {
        private final Image bgImage;

        public BackgroundPanel(Image bgImage) {
            this.bgImage = bgImage;
            setBackground(THEME_LIGHT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bgImage != null) {
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AnnuaireGUI gui = new AnnuaireGUI();
            gui.setVisible(true);
        });
    }
}
