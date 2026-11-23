package com.travelbook.servicereservations;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableFeignClients
@EnableCaching

public class ServiceReservationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceReservationsApplication.class, args);
    }

}
