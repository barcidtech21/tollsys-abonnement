package com.tollsys.bocom.abonnement.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class Abonnement implements Serializable {

    //10/11/2023  17:18:59
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private String nom,  prenom,  raison_sociale, immatriculation;
    private Timestamp date_creation, dernier_passage;

    private long support;
    private float  solde;

    public Abonnement() {
    }

    public Abonnement(String nom, String prenom, String raison_sociale, String immatriculation, Timestamp date_creation, Timestamp dernier_passage, long support, float solde) {
        this.nom = nom;
        this.prenom = prenom;
        this.raison_sociale = raison_sociale;
        this.immatriculation = immatriculation;
        this.date_creation = date_creation;
        this.dernier_passage = dernier_passage;
        this.support = support;
        this.solde = solde;
    }

    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getRaison_sociale() {
        return raison_sociale;
    }

    public void setRaison_sociale(String raison_sociale) {
        this.raison_sociale = raison_sociale;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    public Timestamp getDate_creation() {
        return date_creation;
    }

    public void setDate_creation(Timestamp date_creation) {
        this.date_creation = date_creation;
    }

    public Timestamp getDernier_passage() {
        return dernier_passage;
    }

    public void setDernier_passage(Timestamp dernier_passage) {
        this.dernier_passage = dernier_passage;
    }

    public long getSupport() {
        return support;
    }

    public void setSupport(long support) {
        this.support = support;
    }

    public float getSolde() {
        return solde;
    }

    public void setSolde(float solde) {
        this.solde = solde;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Abonnement)) return false;
        Abonnement that = (Abonnement) o;
        return getSupport() == that.getSupport() && Float.compare(getSolde(), that.getSolde()) == 0 && Objects.equals(getDateFormat(), that.getDateFormat()) && Objects.equals(getNom(), that.getNom()) && Objects.equals(getPrenom(), that.getPrenom()) && Objects.equals(getRaison_sociale(), that.getRaison_sociale()) && Objects.equals(getImmatriculation(), that.getImmatriculation()) && Objects.equals(getDate_creation(), that.getDate_creation()) && Objects.equals(getDernier_passage(), that.getDernier_passage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDateFormat(), getNom(), getPrenom(), getRaison_sociale(), getImmatriculation(), getDate_creation(), getDernier_passage(), getSupport(), getSolde());
    }

    // Method to return a CSV line representation of the object
    public String toCsvLine() {
        String dateCreationStr = date_creation != null ? dateFormat.format(date_creation) : "";
        String dernierPassageStr = dernier_passage != null ? dateFormat.format(dernier_passage) : "";

        return String.join(",",
                escapeCsv(nom),
                escapeCsv(prenom),
                escapeCsv(raison_sociale),
                escapeCsv(immatriculation),
                escapeCsv(dateCreationStr),
                escapeCsv(dernierPassageStr),
                String.valueOf(support),
                String.format("%.2f", solde)
        );
    }

    // Helper method to handle special characters in CSV values
    private String escapeCsv(String value) {
        return "\"" + (value != null ? value.replace("\"", "\"\"") : "") + "\"";
    }

}
