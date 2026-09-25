package com.logiflow.tms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Tâches planifiées (analyse de maintenance prédictive de nuit). */
@Configuration
@EnableScheduling
public class SchedulingConfig {}
