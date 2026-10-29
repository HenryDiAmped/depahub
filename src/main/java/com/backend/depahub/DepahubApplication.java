package com.backend.depahub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DepahubApplication {

    public static void main(String[] args) {
        SpringApplication.run(DepahubApplication.class, args);
    }

}
