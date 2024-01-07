package com.tollsys.bocom.abonnement.mapper;

import com.tollsys.bocom.abonnement.entity.Abonnement;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AbonnementMapper implements RowMapper<Abonnement> {
    @Override
    public Abonnement mapRow(ResultSet rs, int rowNum) throws SQLException {
        Abonnement abonnement = new Abonnement();
        abonnement.setNom(rs.getString("nom"));
        abonnement.setPrenom(rs.getString("prenom"));
        abonnement.setRaison_sociale(rs.getString("raison_sociale"));
        abonnement.setImmatriculation(rs.getString("immatriculation"));
        abonnement.setSolde(rs.getFloat("solde"));
        abonnement.setDate_creation(rs.getTimestamp("date_creation"));
        abonnement.setDernier_passage(rs.getTimestamp("dernier_passage"));
        abonnement.setSupport(rs.getLong("support"));
        return abonnement;
    }
}
