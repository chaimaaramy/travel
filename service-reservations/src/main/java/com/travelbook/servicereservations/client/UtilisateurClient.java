package com.travelbook.servicereservations.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "service-utilisateurs")
public interface UtilisateurClient {

    @GetMapping("/api/users/{id}")
    Map<String, Object> getUser(@PathVariable("id") Long id);
}