package com.logiflow.tms.shared;

import java.nio.file.Path;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Base des tests d'intégration : un unique conteneur PostgreSQL/PostGIS/pgvector (construit depuis
 * {@code docker/postgres/Dockerfile}, pour rester représentatif de l'environnement réel) est
 * démarré une seule fois et partagé par toutes les sous-classes. Chaque test s'exécute dans une
 * transaction annulée à la fin (pas d'écriture persistée entre les tests).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public abstract class AbstractIntegrationTest {

  protected static final PostgreSQLContainer POSTGRES;

  static {
    // ImageFromDockerfile étend LazyFuture<String> (Testcontainers 2.x) : get() résout l'image de
    // façon synchrone et ne déclare plus d'exception vérifiée (contrairement à
    // java.util.concurrent.Future).
    ImageFromDockerfile image =
        new ImageFromDockerfile("logiflow/postgres-postgis-pgvector-test", false)
            .withDockerfile(Path.of("docker/postgres/Dockerfile"));

    POSTGRES =
        new PostgreSQLContainer(
                DockerImageName.parse(image.get()).asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("logiflow_test")
            .withUsername("logiflow")
            .withPassword("logiflow_test");
    POSTGRES.start();
  }

  @DynamicPropertySource
  static void proprietesDynamiques(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }
}
