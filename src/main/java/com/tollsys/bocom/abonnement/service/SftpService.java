package com.tollsys.bocom.abonnement.service;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Service
public class SftpService {

    private static final Logger log = LoggerFactory.getLogger(SftpService.class);
    @Value("${app.sftp.host}")
    private String sftpHost;

    @Value("${app.sftp.user}")
    private String sftpUser;

    @Value("${app.sftp.pwd}")
    private String sftpPwd;

    @Value("${app.sftp.port}")
    private int sftpPort;

    @Value("${app.sftp.privateKey}")
    private String privateKeyPath;

    @Value("${app.sftp.passphrase}")
    private String passphrase; // Passphrase for the private key, if any


    private String privateKeyPath(){
        String data = "";
        ClassPathResource cpr = new ClassPathResource("security/id_rsa");
        try {
            byte[] bdata = FileCopyUtils.copyToByteArray(cpr.getInputStream());
            data = new String(bdata, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("private key SFTP not found: ", e);
        }

        return data;
    }

//    public static void main(String[] args)
public boolean uploadFile(File csv, String remoteDir) {
    ChannelSftp channelSftp = null;
    try {
        channelSftp = setupJsch();

        // Ensure the remote directory exists
        String[] folders = remoteDir.split("/");
        String path = "";
        for (String folder : folders) {
            if (!folder.isEmpty()) {
                path += "/" + folder;
                try {
                    channelSftp.cd(path);
                } catch (Exception e) {
                    channelSftp.mkdir(path);
                }
            }
        }

        // Upload the file
        try (InputStream inputStream = new FileInputStream(csv)) {
            channelSftp.put(inputStream, path + "/" + csv.getName());
            log.info("File uploaded successfully - " + path + "/" + csv.getName());
        }
    } catch (Exception e) {
        log.error("Error uploading file: ", e);
        return false;
    } finally {
        disconnectSftpChannel(channelSftp);
    }
    return true;
}


    private ChannelSftp setupJsch() throws Exception {
        JSch jsch = new JSch();
        log.info("[ABONNEMENT][INFO] - SSH key exists ? :  {}, SSH Key Path: {} ",
                new File(privateKeyPath).exists(),
                new File(privateKeyPath).getAbsolutePath());
//        jsch.addIdentity(privateKeyPath);
        Session session = jsch.getSession(sftpUser, sftpHost, sftpPort);
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setPassword(sftpPwd);
        session.connect();

        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
        return channel;
    }

    private void disconnectSftpChannel(ChannelSftp channelSftp) {
        if (channelSftp != null) {
            try {
                Session session = channelSftp.getSession();
                channelSftp.exit();
                if (session != null) {
                    session.disconnect();
                }
            } catch (Exception e) {
                log.error("Error disconnecting SFTP channel: ", e);
            }
        }
    }
}
