package com.tfind.toilet.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String projectDir = System.getProperty("user.dir");
        File envFile = new File(projectDir, ".env");
        File envExampleFile = new File(projectDir, ".env.example");

        if (!envFile.exists()) {
            System.out.println(".env file not found, attempting to copy from .env.example");

            if (envExampleFile.exists()) {
                try {
                    Files.copy(envExampleFile.toPath(), envFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Successfully copied .env.example to .env");
                    System.out.println("Please edit .env file and fill in your API keys before starting the application");
                } catch (IOException e) {
                    System.err.println("Failed to copy .env.example to .env: " + e.getMessage());
                }
            } else {
                System.err.println(".env.example file not found. Please create .env file manually with your API keys");
            }
        }

        Dotenv dotenv = Dotenv.configure()
                .directory(projectDir)
                .ignoreIfMissing()
                .load();

        Map<String, Object> envMap = new HashMap<>();
        dotenv.entries().forEach(entry -> {
            envMap.put(entry.getKey(), entry.getValue());
            System.setProperty(entry.getKey(), entry.getValue());
        });

        environment.getPropertySources().addFirst(new MapPropertySource("dotenv", envMap));
    }
}
