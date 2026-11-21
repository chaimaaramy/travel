
package com.travelbook.servicereservations.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long voyageId;

    @Column(nullable = false)
    private Integer nombrePersonnes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateReservation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutReservation statut;
}
