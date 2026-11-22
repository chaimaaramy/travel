package com.travelbook.servicepaiements.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "service-reservations")
public interface ReservationClient {

    @GetMapping("/api/reservations/{id}")
    ReservationDTO getReservation(@PathVariable("id") Long id);

    @PostMapping("/api/reservations/{id}/confirmer")
    ReservationDTO confirmerReservation(@PathVariable("id") Long id);

    @PostMapping("/api/reservations/{id}/annuler")
    ReservationDTO annulerReservation(@PathVariable("id") Long id);
}
