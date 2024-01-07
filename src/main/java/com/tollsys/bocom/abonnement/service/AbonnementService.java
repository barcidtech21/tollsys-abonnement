package com.tollsys.bocom.abonnement.service;

import com.tollsys.bocom.abonnement.entity.Abonnement;
import com.tollsys.bocom.abonnement.repository.AbonnementRepository;
import com.tollsys.bocom.abonnement.utils.CsvWriterUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static com.tollsys.bocom.abonnement.common.Const.*;

@Service
public class AbonnementService {

    private static final Logger log = LoggerFactory.getLogger(AbonnementService.class);

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
    private final DateTimeFormatter dirFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Value("${app.csv.output.dir}")
    private String outputDir;

    @Value("${app.sftp.minfi.dir}")
    private String sftpMinfiDir;

    @Value("${app.sftp.erocam.dir}")
    private String sftpErocamDir;

    private final AbonnementRepository abonnementRepository;
    private final SftpService sftpService;

    public AbonnementService(AbonnementRepository abonnementRepository, SftpService sftpService) {
        this.abonnementRepository = abonnementRepository;
        this.sftpService = sftpService;
    }

    private String createDirectoryFromDateTime(LocalDateTime dateDePassage) {
        // Extract year, month, and day from dateDePassage
        int year = dateDePassage.getYear();
        int month = dateDePassage.getMonthValue();
        int day = dateDePassage.getDayOfMonth();

        // Construct the directory path
        String directoryPath = String.format(outputDir + "/%d/%d/%d", year, month, day);


        try {
            // Create the directory if it does not exist
            Files.createDirectories(Paths.get(directoryPath));
            log.info("Dossier {} crée: ", directoryPath);
            return directoryPath;
        } catch (IOException e) {
            log.error("Erreur de création de dossion d'extraction des CSV, l'extraction sera sur le dossier {}", outputDir, e);
        }
        return outputDir;
    }

    public File createCSvFile(LocalDateTime dateDePassage) throws DataAccessException, IOException {
        List<Abonnement> abonnements = abonnementRepository.fetchData(dateDePassage);
        String outputPath = createDirectoryFromDateTime(dateDePassage);
        String csvName = ABONNEMENT_PREFIX + dateDePassage.format(formatter) + CSV_EXT;
        String md5Name = ABONNEMENT_PREFIX + dateDePassage.format(formatter) + CSV_MD5_EXT;
        String csvPath = outputPath + "/" + csvName;
        String md5Path = outputPath + "/" + md5Name;
        String auditPath = outputDir + "/" + AUDIT_PREFIX + dateDePassage.format(dirFormatter) + TXT_EXT;
        File csv = new File(csvPath);
        File md5 = new File(md5Path);
        File auditFile = new File(auditPath);
        if (!auditFile.exists()) {
            auditFile.createNewFile();
            FileUtils.writeStringToFile(auditFile, "########################################## Audit du " + dateDePassage.format(dirFormatter) + " ##########################################" + System.lineSeparator(), StandardCharsets.UTF_8, true);
        }
        if (!md5.exists()) {
            md5.createNewFile();
        }
        CsvWriterUtils.writeRedditionListToCsv(abonnements,
                csv);

        String md5Hash = getMd5Hash(csvPath);
        FileUtils.writeStringToFile(md5, md5Hash + " " + csvName + System.lineSeparator(), StandardCharsets.UTF_8, true);


        String auditContent = String.format("date=%s ; fichier_généré=%s ; md5=%s ; nb_lignes=%d ; taille=%s",
                dateDePassage.toString(),
                csvName,
                md5Hash,
                abonnements.size(),
                FileUtils.byteCountToDisplaySize(csv.length()));
        final StringBuilder sb = new StringBuilder();
        sb.append(auditContent);

        String zipPath = outputPath + "/" + ABONNEMENT_PREFIX + dateDePassage.format(formatter) + ZIP_EXT;
        createZipFile(new String[]{csvPath, md5Path}, zipPath);
        File zip = new File(zipPath);

        List<String> dirPaths = getFtpDirPath(dateDePassage);
        dirPaths.forEach(dirPath -> {
            boolean isCsvUploaded = sftpService.uploadFile(csv, dirPath);
            if (isCsvUploaded) {
                sb.append(String.format(" ; Upload SFTP du csv dans  %s OK ", dirPath));
            } else {
                sb.append(String.format(" ; Upload SFTP du csv dans  %s KO ", dirPath));
            }
            boolean isZipUploaded = sftpService.uploadFile(zip, dirPath);
            if (isZipUploaded) {
                sb.append(String.format(" ; Upload SFTP du zip md5 dans  %s OK ", dirPath));
            } else {
                sb.append(String.format(" ; Upload SFTP du zip md5 dans  %s KO ", dirPath));
            }
        });

        sftpService.uploadFile(auditFile,sftpMinfiDir);
        sftpService.uploadFile(auditFile,sftpErocamDir);


        FileUtils.writeStringToFile(auditFile, sb.toString() + System.lineSeparator(), StandardCharsets.UTF_8, true);


        return csv;

    }

    public  void createZipFile(String[] sourceFiles, String outputZipFile) {
        try (
                FileOutputStream fos = new FileOutputStream(outputZipFile);
                ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(fos)
        ) {
            for (String sourceFile : sourceFiles) {
                addToZipFile(sourceFile, zaos);
            }
        } catch (IOException e) {
            log.error("Erreur de création de ZIP", e);
        }
    }

    private  void addToZipFile(String fileName, ZipArchiveOutputStream zaos) throws IOException {
        File file = new File(fileName);
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)
        ) {
            ZipArchiveEntry zipEntry = new ZipArchiveEntry(file, file.getName());
            zaos.putArchiveEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int count;
            while ((count = bis.read(buffer)) != -1) {
                zaos.write(buffer, 0, count);
            }

            zaos.closeArchiveEntry();
        }
    }

    private static String getMd5Hash(String path) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            File csv = new File(path);
            int size = csv.exists() ? Integer.parseInt(String.valueOf(csv.length())) : 1024;
            try (FileInputStream fis = new FileInputStream(path)) {
                byte[] byteArray = new byte[size];
                int bytesCount;

                while ((bytesCount = fis.read(byteArray)) != -1) {
                    digest.update(byteArray, 0, bytesCount);
                }

                byte[] bytes = digest.digest();
                StringBuilder sb = new StringBuilder();
                for (byte aByte : bytes) {
                    sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
                }
                return sb.toString();
            } catch (IOException e) {
                log.error("Fichier inexistant: ", e);
            }
        } catch (NoSuchAlgorithmException e) {
            log.error("Problème d'hahsage MD5 ", e);
        }
        return StringUtils.EMPTY;
    }

    private List<String> getFtpDirPath(LocalDateTime dateDePassage) {
        String[] dirs = dateDePassage.format(dirFormatter).split("-");
        //avoid elements starting by 0
        for (int i = 0; i < dirs.length; i++) {
            dirs[i] = String.valueOf(Integer.parseInt(dirs[i]));
        }
        String filePath = dirs[0] + "/" + dirs[1] + "/" + dirs[2];
        return Arrays.asList(sftpMinfiDir + filePath, sftpErocamDir + filePath);
    }



}
