package projetAnnuaire;

import java.io.Serializable;

public class Personne implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nom;
    private String prenom;
    private Categorie categorie;
    private String matricule;   // pour les étudiants
    private String email;
    private String telephone;   // pour prof / auxiliaire
    private String domaine;     // domaine d’activité
    private boolean listeRouge;

    public Personne(String nom, String prenom, Categorie categorie,
                    String matricule, String email, String telephone,
                    String domaine, boolean listeRouge) {
        this.nom = nom;
        this.prenom = prenom;
        this.categorie = categorie;
        this.matricule = matricule;
        this.email = email;
        this.telephone = telephone;
        this.domaine = domaine;
        this.listeRouge = listeRouge;
    }

    // Copie contenant uniquement nom, prénom et info "SUR LISTE ROUGE"
    public Personne copieListeRouge() {
        return new Personne(
                this.nom,
                this.prenom,
                this.categorie,
                null,                 // pas de matricule
                null,                 // pas d'email
                null,                 // pas de téléphone
                "SUR LISTE ROUGE",
                true
        );
    }

    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public Categorie getCategorie() { return categorie; }
    public String getMatricule() { return matricule; }
    public String getEmail() { return email; }
    public String getTelephone() { return telephone; }
    public String getDomaine() { return domaine; }
    public boolean isListeRouge() { return listeRouge; }

    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }
    public void setMatricule(String matricule) { this.matricule = matricule; }
    public void setEmail(String email) { this.email = email; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setDomaine(String domaine) { this.domaine = domaine; }
    public void setListeRouge(boolean listeRouge) { this.listeRouge = listeRouge; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Nom : ").append(nom)
          .append(", Prénom : ").append(prenom)
          .append(", Catégorie : ").append(categorie);

        if (matricule != null && !matricule.isEmpty()) {
            sb.append(", Matricule : ").append(matricule);
        }
        if (email != null && !email.isEmpty()) {
            sb.append(", Email : ").append(email);
        }
        if (telephone != null && !telephone.isEmpty()) {
            sb.append(", Téléphone : ").append(telephone);
        }
        if (domaine != null && !domaine.isEmpty()) {
            sb.append(", Domaine : ").append(domaine);
        }
        if (listeRouge) {
            sb.append(" [LISTE ROUGE]");
        }

        return sb.toString();
    }
}
