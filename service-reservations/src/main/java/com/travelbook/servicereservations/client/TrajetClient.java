package com.travelbook.servicereservations.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "service-trajets")
public interface TrajetClient {

    @GetMapping("/api/trajets/{id}")
    TrajetDTO getTrajet(@PathVariable("id") Long id);

    @PostMapping("/api/trajets/{id}/reserver-places")
    void reserverPlaces(@PathVariable("id") Long id, @RequestParam("nombre") int nombre);

    @PostMapping("/api/trajets/{id}/liberer-places")
    void libererPlaces(@PathVariable("id") Long id, @RequestParam("nombre") int nombre);
}