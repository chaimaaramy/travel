package com.travelbook.servicereservations;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableFeignClients

public class ServiceReservationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceReservationsApplication.class, args);
    }

}
