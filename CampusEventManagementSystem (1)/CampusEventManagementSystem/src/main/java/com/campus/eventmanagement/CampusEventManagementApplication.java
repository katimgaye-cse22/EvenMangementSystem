package com.campus.eventmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Campus Event Management System.
 *
 * This is a layered Spring Boot application:
 * Controller -> Service -> Repository -> Entity (JPA/Hibernate) -> MySQL
 */
@SpringBootApplication
public class CampusEventManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusEventManagementApplication.class, args);
    }

}
