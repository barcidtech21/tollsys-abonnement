package com.tollsys.bocom.abonnement.schedule;

import com.tollsys.bocom.abonnement.service.AbonnementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

@Component
public class CsvScheduler implements SchedulingConfigurer{

    private static final Logger log = LoggerFactory.getLogger(CsvScheduler.class);

    @Value("${app.task.cron}")
    private String cron;

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;

    private final AbonnementService abonnementService;


    public CsvScheduler(ThreadPoolTaskScheduler threadPoolTaskScheduler, AbonnementService abonnementService) {
        this.threadPoolTaskScheduler = threadPoolTaskScheduler;
        this.abonnementService = abonnementService;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // Set the task scheduler to the registrar
        taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);

        taskRegistrar.addTriggerTask(new Runnable() {
            @Override
            public void run() {
                LocalDateTime now = LocalDateTime.now(); // Example LocalDateTime

                log.info("Abonnement à : {}", now);
                try {
                    abonnementService.createCSvFile(now);
                    log.info("csv de Abonnement à : {} est crée", now);
                } catch (IOException e) {
                    log.error("Erreur : {}", e.getMessage());
                }
            }
        }, new Trigger() {
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                CronTrigger cronTrigger = new CronTrigger(getCron());
                return cronTrigger.nextExecutionTime(triggerContext);
            }

        });
    }
}
