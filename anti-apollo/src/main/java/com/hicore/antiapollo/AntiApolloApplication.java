package com.hicore.antiapollo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AntiApolloApplication {

    public static void main(String[] args) {
        System.setProperty("env", "DEV");
        SpringApplication.run(AntiApolloApplication.class, args);
    }

}
