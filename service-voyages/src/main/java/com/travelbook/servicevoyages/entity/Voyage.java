package com.travelbook.servicevoyages.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "voyages")
public class Voyage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private  String destination;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private LocalDate dateDepart;
    @Column(nullable = false)
    private LocalDate dateRetour;
    @Column(nullable = false,precision = 10,scale = 2)
    private BigDecimal prix;


}
