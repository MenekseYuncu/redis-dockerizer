package com.redisdockerizer.keymanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

/**
 * The KeyManagementApplication class serves as the entry point
 * for the Spring Boot application. It is annotated with
 * {@code @SpringBootApplication,} which signifies it as the primary configuration
 * class and includes component scanning, autoconfiguration, and
 * property support features of Spring Boot.
 * <p>
 * This application explicitly excludes the SecurityAutoConfiguration class,
 * thereby disabling the default Spring Security configuration.
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class KeyManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(KeyManagementApplication.class, args);
    }

}
