package com.travelbook.servicepaiements.service;

import com.travelbook.servicepaiements.client.ReservationClient;
import com.travelbook.servicepaiements.client.ReservationDTO;
import com.travelbook.servicepaiements.client.UtilisateurClient;
import com.travelbook.servicepaiements.client.UtilisateurDTO;
import com.travelbook.servicepaiements.dto.NotificationEmailDto;
import com.travelbook.servicepaiements.dto.PaiementRequestDTO;
import com.travelbook.servicepaiements.entity.ModePaiement;
import com.travelbook.servicepaiements.entity.Paiement;
import com.travelbook.servicepaiements.entity.StatutPaiement;
import com.travelbook.servicepaiements.messaging.NotificationPublisher;
import com.travelbook.servicepaiements.repository.PaiementRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaiementServiceTest {

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private ReservationClient reservationClient;

    @Mock
    private UtilisateurClient utilisateurClient;

    @Mock
    private NotificationPublisher notificationPublisher;

    @InjectMocks
    private PaiementService paiementService;

    @BeforeEach
    void startTransactionSynchronization() {
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void clearTransactionSynchronization() {
        TransactionSynchronizationManager.clearSynchronization();
    }

    @Test
    void createsPaymentOnlyForMatchingPendingReservation() {
        when(reservationClient.getReservation(12L)).thenReturn(reservation(
                12L, "EN_ATTENTE", "75.00"));
        when(paiementRepository.findByReservationId(12L)).thenReturn(Optional.empty());
        when(paiementRepository.save(any(Paiement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = paiementService.createPaiement(request(
                12L, "75.00", StatutPaiement.EN_ATTENTE));

        assertEquals(StatutPaiement.EN_ATTENTE, result.statut());
        assertEquals(0, new BigDecimal("75.00").compareTo(result.montant()));
        verify(reservationClient).getReservation(12L);
    }

    @Test
    void rejectsPaymentWhenReservationAmountDoesNotMatch() {
        when(reservationClient.getReservation(12L)).thenReturn(reservation(
                12L, "EN_ATTENTE", "80.00"));

        var error = assertThrows(ResponseStatusException.class,
                () -> paiementService.createPaiement(request(
                        12L, "75.00", StatutPaiement.EN_ATTENTE)));

        assertEquals(400, error.getStatusCode().value());
    }

    @Test
    void rejectsSecondPaymentForReservation() {
        when(paiementRepository.findByReservationId(12L))
                .thenReturn(Optional.of(paiement(4L, 12L, StatutPaiement.EN_ATTENTE)));

        var error = assertThrows(ResponseStatusException.class,
                () -> paiementService.createPaiement(request(
                        12L, "75.00", StatutPaiement.EN_ATTENTE)));

        assertEquals(409, error.getStatusCode().value());
        verify(reservationClient, never()).getReservation(12L);
    }

    @Test
    void paidPaymentConfirmsReservationAndPublishesEmailAfterCommit() {
        Paiement paiement = paiement(4L, 12L, StatutPaiement.EN_ATTENTE);
        when(paiementRepository.findById(4L)).thenReturn(Optional.of(paiement));
        when(reservationClient.getReservation(12L)).thenReturn(reservation(
                12L, "EN_ATTENTE", "75.00"));
        when(reservationClient.confirmerReservation(12L)).thenReturn(reservation(
                12L, "CONFIRMEE", "75.00"));
        when(utilisateurClient.getUtilisateur(8L)).thenReturn(new UtilisateurDTO(
                8L, "Utilisateur test", "test@example.com", "CLIENT", true));
        when(paiementRepository.save(paiement)).thenReturn(paiement);

        var result = paiementService.updatePaiement(4L, request(
                12L, "75.00", StatutPaiement.PAYE));

        assertEquals(StatutPaiement.PAYE, result.statut());
        verify(reservationClient).confirmerReservation(12L);
        verify(notificationPublisher, never()).envoyer(any(NotificationEmailDto.class));

        runAfterCommit();

        ArgumentCaptor<NotificationEmailDto> email =
                ArgumentCaptor.forClass(NotificationEmailDto.class);
        verify(notificationPublisher).envoyer(email.capture());
        assertEquals("test@example.com", email.getValue().destinataire());
        assertEquals("Paiement confirmé", email.getValue().objet());
        org.junit.jupiter.api.Assertions.assertTrue(
                email.getValue().contenu().contains("réservation 12 est confirmée"));
    }

    @Test
    void refusedPaymentCancelsReservationAndSchedulesEmail() {
        Paiement paiement = paiement(4L, 12L, StatutPaiement.EN_ATTENTE);
        when(paiementRepository.findById(4L)).thenReturn(Optional.of(paiement));
        when(reservationClient.getReservation(12L)).thenReturn(reservation(
                12L, "EN_ATTENTE", "75.00"));
        when(reservationClient.annulerReservation(12L)).thenReturn(reservation(
                12L, "ANNULEE", "75.00"));
        when(utilisateurClient.getUtilisateur(8L)).thenReturn(new UtilisateurDTO(
                8L, "Utilisateur test", "test@example.com", "CLIENT", true));
        when(paiementRepository.save(paiement)).thenReturn(paiement);

        paiementService.updatePaiement(4L, request(
                12L, "75.00", StatutPaiement.REFUSE));

        verify(reservationClient).annulerReservation(12L);
        runAfterCommit();

        ArgumentCaptor<NotificationEmailDto> email =
                ArgumentCaptor.forClass(NotificationEmailDto.class);
        verify(notificationPublisher).envoyer(email.capture());
        assertEquals("Paiement refusé", email.getValue().objet());
        org.junit.jupiter.api.Assertions.assertTrue(
                email.getValue().contenu().contains("paiement pour la réservation 12 a été refusé"));
    }

    @Test
    void notificationPublicationFailureDoesNotFailCommittedPayment() {
        Paiement paiement = paiement(4L, 12L, StatutPaiement.EN_ATTENTE);
        when(paiementRepository.findById(4L)).thenReturn(Optional.of(paiement));
        when(reservationClient.getReservation(12L)).thenReturn(reservation(
                12L, "EN_ATTENTE", "75.00"));
        when(reservationClient.confirmerReservation(12L)).thenReturn(reservation(
                12L, "CONFIRMEE", "75.00"));
        when(utilisateurClient.getUtilisateur(8L)).thenReturn(new UtilisateurDTO(
                8L, "Utilisateur test", "test@example.com", "CLIENT", true));
        when(paiementRepository.save(paiement)).thenReturn(paiement);
        doThrow(new AmqpException("Broker indisponible"))
                .when(notificationPublisher).envoyer(any(NotificationEmailDto.class));

        var result = paiementService.updatePaiement(4L, request(
                12L, "75.00", StatutPaiement.PAYE));

        assertEquals(StatutPaiement.PAYE, result.statut());
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(this::runAfterCommit);
    }

    @Test
    void returnsPaymentByReservationId() {
        when(paiementRepository.findByReservationId(12L))
                .thenReturn(Optional.of(paiement(4L, 12L, StatutPaiement.PAYE)));

        assertEquals(4L, paiementService.getPaiementByReservationId(12L).id());
    }

    @Test
    void returnsNotFoundWhenReservationHasNoPayment() {
        when(paiementRepository.findByReservationId(12L)).thenReturn(Optional.empty());

        var error = assertThrows(ResponseStatusException.class,
                () -> paiementService.getPaiementByReservationId(12L));

        assertEquals(404, error.getStatusCode().value());
    }

    private static PaiementRequestDTO request(
            Long reservationId, String amount, StatutPaiement status) {
        return new PaiementRequestDTO(
                reservationId, new BigDecimal(amount), ModePaiement.CARTE_BANCAIRE, status);
    }

    private static ReservationDTO reservation(
            Long id, String status, String amount) {
        return new ReservationDTO(
                id, 8L, 20L, 1, status, new BigDecimal(amount));
    }

    private static Paiement paiement(
            Long id, Long reservationId, StatutPaiement status) {
        Paiement paiement = new Paiement();
        paiement.setId(id);
        paiement.setReservationId(reservationId);
        paiement.setMontant(new BigDecimal("75.00"));
        paiement.setModePaiement(ModePaiement.CARTE_BANCAIRE);
        paiement.setStatut(status);
        return paiement;
    }

    private void runAfterCommit() {
        for (TransactionSynchronization synchronization
                : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }
    }
}
