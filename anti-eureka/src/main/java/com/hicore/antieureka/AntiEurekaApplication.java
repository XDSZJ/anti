package com.hicore.antieureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class AntiEurekaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AntiEurekaApplication.class, args);
    }

}
