package com.travelbook.servicereservations.service;

import com.travelbook.servicereservations.client.PaiementClient;
import com.travelbook.servicereservations.client.TrajetClient;
import com.travelbook.servicereservations.client.TrajetDTO;
import com.travelbook.servicereservations.client.UtilisateurClient;
import com.travelbook.servicereservations.dto.ReservationRequestDTO;
import com.travelbook.servicereservations.entity.Reservation;
import com.travelbook.servicereservations.entity.StatutReservation;
import com.travelbook.servicereservations.repository.ReservationRepository;
import com.travelbook.servicereservations.repository.TicketRepository;
import com.travelbook.servicereservations.resilience.ResilientRequests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UtilisateurClient utilisateurClient;

    @Mock
    private TrajetClient trajetClient;

    @Mock
    private PaiementClient paiementClient;

    @Mock
    private ResilientRequests resilientRequests;

    @InjectMocks
    private ReservationService reservationService;

    @BeforeEach
    void executerLesAppelsDistantDirectement() {
        lenient().when(resilientRequests.lectureOptionnelle(anyString(), any()))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(1)).get());
        lenient().when(resilientRequests.lectureObligatoire(anyString(), any()))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(1)).get());
        lenient().when(resilientRequests.ecritureObligatoire(anyString(), any()))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(1)).get());
    }

    @Test
    void createsReservationAfterVerifyingUserAndReservingSeats() {
        when(reservationRepository.findByIdempotencyKey("booking-31"))
                .thenReturn(Optional.empty());
        when(utilisateurClient.getUser(8L)).thenReturn(Map.of("id", 8L));
        when(trajetClient.getTrajet(20L)).thenReturn(new TrajetDTO(
                20L, 20L, "Paris", "Lyon",
                LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                "TRAIN", new BigDecimal("75.00"), 10));
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> {
                    Reservation reservation = invocation.getArgument(0);
                    reservation.setId(31L);
                    return reservation;
                });

        var result = reservationService.createReservation(
                request(StatutReservation.EN_ATTENTE), "booking-31");

        assertEquals(31L, result.id());
        assertEquals(0, new BigDecimal("75.00").compareTo(result.montant()));
        assertEquals(StatutReservation.EN_ATTENTE, result.statut());
        verify(trajetClient).reserverPlaces(20L, 1);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void repeatedIdempotencyKeyReturnsExistingReservation() {
        Reservation reservation = reservation(31L);
        reservation.setIdempotencyKey("booking-31");
        when(reservationRepository.findByIdempotencyKey("booking-31"))
                .thenReturn(Optional.of(reservation));

        var result = reservationService.createReservation(
                request(StatutReservation.EN_ATTENTE), "booking-31");

        assertEquals(31L, result.id());
        verify(utilisateurClient, never()).getUser(any());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void rejectsNewReservationWithNonPendingStatus() {
        var error = assertThrows(ResponseStatusException.class,
                () -> reservationService.createReservation(
                        request(StatutReservation.CONFIRMEE), "booking-32"));

        assertEquals(400, error.getStatusCode().value());
        verify(utilisateurClient, never()).getUser(any());
    }

    @Test
    void enrichedLookupIncludesRemotePaymentAndTravelerDetails() {
        Reservation reservation = reservation(31L);
        when(reservationRepository.findById(31L)).thenReturn(Optional.of(reservation));
        when(utilisateurClient.getUser(8L)).thenReturn(Map.of("id", 8L));
        when(trajetClient.getTrajet(20L)).thenReturn(new TrajetDTO(
                20L, 20L, "Paris", "Lyon",
                LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                "TRAIN", new BigDecimal("75.00"), 10));
        when(ticketRepository.findByReservationId(31L)).thenReturn(java.util.List.of());
        when(paiementClient.getPaiementParReservation(31L))
                .thenReturn(Map.of("id", 5L, "statut", "PAYE"));

        var details = reservationService.getDetails(31L);

        assertEquals(Map.of("id", 8L), details.utilisateur());
        assertEquals("Lyon", details.trajet().villeArrivee());
        assertEquals(Map.of("id", 5L, "statut", "PAYE"), details.paiement());
    }

    private static ReservationRequestDTO request(StatutReservation status) {
        return new ReservationRequestDTO(8L, 20L, 1, status);
    }

    private static Reservation reservation(Long id) {
        Reservation reservation = new Reservation();
        reservation.setId(id);
        reservation.setUserId(8L);
        reservation.setVoyageId(20L);
        reservation.setNombrePersonnes(1);
        reservation.setDateReservation(LocalDateTime.now());
        reservation.setStatut(StatutReservation.EN_ATTENTE);
        reservation.setMontant(new BigDecimal("75.00"));
        return reservation;
    }
}
