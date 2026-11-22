package com.travelbook.servicepaiements;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ServicePaiementsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicePaiementsApplication.class, args);
    }

}
