package com.tfind.toilet.config;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.File;

@Configuration
public class BerkeleyDbConfig {

    @Value("${berkeleydb.dir}")
    private String dbDir;

    private Environment environment;

    @Bean
    public Environment berkeleyEnvironment() {
        File dir = new File(dbDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        envConfig.setTransactional(true);
        environment = new Environment(dir, envConfig);
        return environment;
    }

    @Bean
    public Database toiletsDatabase(Environment berkeleyEnvironment) {
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setTransactional(true);
        return berkeleyEnvironment.openDatabase(null, "toilets", dbConfig);
    }

    @Bean
    public Database userProfilesDatabase(Environment berkeleyEnvironment) {
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setTransactional(true);
        return berkeleyEnvironment.openDatabase(null, "user_profiles", dbConfig);
    }

    @Bean
    public Database scoreRecordsDatabase(Environment berkeleyEnvironment) {
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setTransactional(true);
        return berkeleyEnvironment.openDatabase(null, "score_records", dbConfig);
    }

    @Bean
    public Database reviewsDatabase(Environment berkeleyEnvironment) {
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setTransactional(true);
        return berkeleyEnvironment.openDatabase(null, "reviews", dbConfig);
    }

    @Bean
    public Database reportsDatabase(Environment berkeleyEnvironment) {
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setTransactional(true);
        return berkeleyEnvironment.openDatabase(null, "reports", dbConfig);
    }

    @Bean
    public Database reviewUpdatesDatabase(Environment berkeleyEnvironment) {
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setTransactional(true);
        return berkeleyEnvironment.openDatabase(null, "review_updates", dbConfig);
    }

    @PreDestroy
    public void close() {
        if (environment != null) {
            environment.sync();
            environment.close();
        }
    }
}
