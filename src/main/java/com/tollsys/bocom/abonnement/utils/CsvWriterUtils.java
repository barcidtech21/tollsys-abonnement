package com.tollsys.bocom.abonnement.utils;

import com.tollsys.bocom.abonnement.entity.Abonnement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvWriterUtils {

    private static final Logger log = LoggerFactory.getLogger(CsvWriterUtils.class);

    public static void writeRedditionListToCsv(List<Abonnement> abonnementList, File csv) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csv))) {
            // Write header
            writer.write("nom,prenom,raison_social,immatriculation,date_creation,dernier_passage,support,solde");
            writer.newLine();
            writer.flush();

            // Write data
            for (Abonnement abonnement : abonnementList) {
                writer.write(abonnement.toCsvLine());
                writer.newLine();
            }
        } catch (IOException e) {
            log.error("Erreur de création de fichier CSV de Abonnement, cause: ", e);
        }
    }
}

