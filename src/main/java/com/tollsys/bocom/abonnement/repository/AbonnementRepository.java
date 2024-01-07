package com.tollsys.bocom.abonnement.repository;

import com.tollsys.bocom.abonnement.entity.Abonnement;
import com.tollsys.bocom.abonnement.mapper.AbonnementMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AbonnementRepository {

    private static final Logger log = LoggerFactory.getLogger(AbonnementRepository.class);

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private ResourceLoader resourceLoader;
    private JdbcTemplate jdbcTemplate;

    private String query;

    public AbonnementRepository(JdbcTemplate jdbcTemplate, ResourceLoader resourceLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    private void init() {

        Resource resource = resourceLoader.getResource("classpath:db.query/query.sql");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            query = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            log.error("Problème de récupération de requête SQL", e);
        }
        log.debug("Abonnement query: {}", query);
    }


    public List<Abonnement> fetchData(LocalDateTime dateDePassage) throws DataAccessException {
        query = query.replace("?", "'" + dateDePassage.format(formatter) + "'");
        return jdbcTemplate.query(query,
                new AbonnementMapper());
    }
}
