package com.tollsys.bocom.abonnement.controller;

import com.tollsys.bocom.abonnement.schedule.CsvScheduler;
import com.tollsys.bocom.abonnement.service.AbonnementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/abonnements")
@EnableScheduling
public class AbonnementController {

    private static final Logger log = LoggerFactory.getLogger(AbonnementController.class);

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter fileNameFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");

    private final AbonnementService abonnementService;
    private final CsvScheduler csvScheduler;


    public AbonnementController(AbonnementService abonnementService, CsvScheduler csvScheduler) {
        this.abonnementService = abonnementService;
        this.csvScheduler = csvScheduler;
    }



    @Operation(summary = "Créer csv des abonnements pour la date de passage donnée et placer le dans le Temp")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Abonnements trouvées et écrite sur un csv dans Temp",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "400", description = "date de passage non correcte",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Pas de abonnement trouvée",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Erreur interne",
                    content = @Content) })
    @GetMapping("/csv")
    public ResponseEntity<String> createCsv( @RequestParam("dateDePassage") String dateString) {
        try {
            if(!isValidDate(dateString)) {
                return ResponseEntity.badRequest()
                        .body("Format de date non correcte, essayer avec ce format: yyyy-MM-dd, exemple: 2023-12-07");
            }
            // Parse the date string to Date object

            // Generate CSV data based on the date
            LocalDate localDate = LocalDate.parse(dateString, formatter);
            LocalTime localTime = LocalTime.now();
            LocalDateTime dateDePassageTime = LocalDateTime.of(localDate, localTime);
            File csv = abonnementService.createCSvFile(dateDePassageTime);

            return ResponseEntity.ok()
                    .body("CSV pour date "+dateString+" crée, veuillez le récupérer dans "+csv.getAbsolutePath());
        } catch (DataAccessException e) {
            log.error("Erreur Base de donnée: ", e);
            return ResponseEntity.status(HttpStatus.valueOf(500))
                    .body("Database Error: "+e.getMessage());
        } catch (Exception e) {
            log.error("Erreur de création du du csv: ", e);
            return ResponseEntity.badRequest()
                    .body(" Error: "+e.getMessage());
        }
    }

    @Operation(summary = "mettre à jours le cron du scheduler")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "cron mis à jour",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "400", description = "cron  erroné",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Pas de réponse",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Erreur interne",
                    content = @Content) })
    @GetMapping("/update-cron")
    public ResponseEntity<String> updateCron( @RequestParam("hours") String cronHours) {
        try {
            String cron = getCron(cronHours);
            if(!CronExpression.isValidExpression(cron)){
                return ResponseEntity.badRequest()
                        .body(" Vérifier le format des Heures, exemple: 8-20 ");
            }

            log.info("nouveau cron :{}", cron);
            csvScheduler.setCron(cron);

            return ResponseEntity.ok()
                    .body("cron mis à jours");
        }  catch (Exception e) {
            return ResponseEntity.status(HttpStatus.valueOf(500))
                    .body(" Error: "+e.getMessage());
        }
    }

    private String getCron(String cronHours) {
        return String.format( "* * %s * * ?",cronHours);
    }


    public static boolean isValidDate(String dateString) {
        try {
            // Parse the dateString - will throw DateTimeParseException if invalid
            LocalDate.parse(dateString, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }


    private static String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
}
