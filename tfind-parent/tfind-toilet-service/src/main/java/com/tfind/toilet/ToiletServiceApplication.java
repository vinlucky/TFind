package com.tfind.toilet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ToiletServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ToiletServiceApplication.class, args);
    }
}
