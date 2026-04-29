package org.antredesloutres.papi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry of the application. It bootstraps the Spring Boot application context and starts the embedded server.
 * Make sure you have the necessary configurations in application.properties and .env files before running the application.
 */
@SpringBootApplication
@Slf4j
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        log.info("API launched");
    }
}
