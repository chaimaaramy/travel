package com.travelbook.servicereservations.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "service-paiements")
public interface PaiementClient {

    @GetMapping("/api/paiements/reservation/{reservationId}")
    Map<String, Object> getPaiementParReservation(@PathVariable("reservationId") Long reservationId);
}