package com.tollsys.bocom.abonnement;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class AbonnementApplication {

    private static final Logger log = LoggerFactory.getLogger(AbonnementApplication.class);

    private String startingCommand() {
        String data = "";
        ClassPathResource cpr = new ClassPathResource("scripts/InitApp.ps1");
        try {
            byte[] bdata = FileCopyUtils.copyToByteArray(cpr.getInputStream());
            data = new String(bdata, StandardCharsets.UTF_8);

        } catch (IOException e) {
            log.error("INIT Script file  not found: ", e);
        }

        return data;
    }


    public static void main(String[] args) {
        SpringApplication.run(AbonnementApplication.class, args);

    }

    @PostConstruct
    private void initShellSSH() {
        try {
            String command = startingCommand();
            log.debug("Start Script: \n {}", command);
            log.info("OS - {}, User - {}", System.getProperty("os.name"), System.getProperty("user.name"));
            ProcessBuilder processBuilder = new ProcessBuilder();
            if (System.getProperty("os.name").startsWith("Windows")) {
                processBuilder.command("powershell.exe", "/c", command);
                processBuilder.redirectErrorStream(true);

            } else {
                command = "PWD";
                processBuilder.command("bash", "-c", "pwd");
                processBuilder.redirectErrorStream(true);
            }
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (StringUtils.isNoneBlank(line)
                        && line.contains("admin_prest@MBK-MINFI")) {
                    log.info("- La clé privée SSH est ajouté à l'agent SSH");
                } else {
                    log.warn("- La clé privée SSH n'est pas ajouté à l'agent, vuillez l'ajouter via la commande ssh-dd");
                }
            }
            int exitCode = process.waitFor();
            log.info("starting script Exited with code: {} ", exitCode);
            log.info("Command {} , STARTED: ", command);

        } catch (IOException e) {
            log.error("Error init SSH command: ", e);
        } catch (InterruptedException e) {
            log.error("Error exec: ", e);
        }
    }

}
