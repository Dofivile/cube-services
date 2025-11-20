package com.example.cube;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // ← to this the process api every set interval
public class CubeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CubeApplication.class, args);
    }
}
