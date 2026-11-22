package com.travelbook.servicepaiements.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "service-utilisateurs")
public interface UtilisateurClient {

    @GetMapping("/api/users/{id}")
    UtilisateurDTO getUtilisateur(@PathVariable("id") Long id);
}
