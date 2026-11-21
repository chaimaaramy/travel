package com.travelbook.serviceavis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "avis")
public class Avis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long voyageId;

    @Column(nullable = false)
    private Integer note;

    @Column(nullable = false, length = 2000)
    private String commentaire;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;
}
