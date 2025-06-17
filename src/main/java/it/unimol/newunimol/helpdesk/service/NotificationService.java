package it.unimol.newunimol.helpdesk.service;

import org.springframework.stereotype.Service;

import it.unimol.newunimol.helpdesk.client.NotificationClient;
import it.unimol.newunimol.helpdesk.dto.NotificationRequest;
import it.unimol.newunimol.helpdesk.model.Ticket;

@Service
public class NotificationService {

    private final NotificationClient notificationClient;

    public NotificationService(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

public void inviaNotificaAssegnazione(Ticket ticket, String token) {
    if (ticket.getAssegnatoA() == null) return;

    String destinatarioId = ticket.getAssegnatoA().getExternalId();

    NotificationRequest notificationRequest = new NotificationRequest();
    notificationRequest.setUserId(destinatarioId);
    notificationRequest.setType("TICKET_ASSIGNED");
    notificationRequest.setTitle("Nuovo ticket assegnato");
    notificationRequest.setMessage("Ti è stato assegnato il ticket: \"" + ticket.getTitolo() + "\".");
    notificationRequest.setReferenceId(ticket.getId());
    notificationRequest.setLink("/tickets/" + ticket.getId());

    notificationClient.inviaNotifica(notificationRequest, token);
}

public void inviaNotificaCambioStato(Ticket ticket, String modificatoDaId, String token) {
    String destinatarioId = ticket.getCreatoDa().getExternalId();

    if (destinatarioId.equals(modificatoDaId)) {
        return;
    }

    NotificationRequest notificationRequest = new NotificationRequest();
    notificationRequest.setUserId(destinatarioId);
    notificationRequest.setType("STATUS_UPDATE");
    notificationRequest.setTitle("Aggiornamento stato del ticket");
    notificationRequest.setMessage("Il ticket \"" + ticket.getTitolo() + "\" è stato aggiornato a stato: " + ticket.getTicketStatus());
    notificationRequest.setReferenceId(ticket.getId());
    notificationRequest.setLink("/tickets/" + ticket.getId());

    notificationClient.inviaNotifica(notificationRequest, token);
}

public void inviaNotificaMessaggio(Ticket ticket, String autoreId, String token) {
    String destinatarioId = null;
    if (ticket.getAssegnatoA() != null) {
        destinatarioId = autoreId.equals(ticket.getAssegnatoA().getExternalId())
            ? ticket.getCreatoDa().getExternalId()
            : ticket.getAssegnatoA().getExternalId();
    }

    if (destinatarioId != null) {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setUserId(destinatarioId);
        notificationRequest.setType("NEW_MESSAGE");
        notificationRequest.setTitle("Nuovo messaggio nel ticket");
        notificationRequest.setMessage("Hai ricevuto un nuovo messaggio nel ticket \"" + ticket.getTitolo() + "\".");
        notificationRequest.setReferenceId(ticket.getId());
        notificationRequest.setLink("/tickets/" + ticket.getId());

        notificationClient.inviaNotifica(notificationRequest, token);
    }
}


}