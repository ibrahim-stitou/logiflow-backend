package com.logiflow.tms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.modulith.Modulithic;

/** Point d'entrée de l'application LogiFlow TMS. */
@Modulithic(systemName = "LogiFlow TMS")
@SpringBootApplication
@ConfigurationPropertiesScan
public class LogiflowApplication {

  public static void main(String[] args) {
    SpringApplication.run(LogiflowApplication.class, args);
  }
}
