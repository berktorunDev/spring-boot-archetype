package com.io.spring_boot_archetype;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication
public class SpringBootArchetypeApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootArchetypeApplication.class, args);
    }
}
