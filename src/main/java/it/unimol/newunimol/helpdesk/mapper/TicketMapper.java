package it.unimol.newunimol.helpdesk.mapper;

import org.springframework.stereotype.Component;

import it.unimol.newunimol.helpdesk.dto.MessageRequest;
import it.unimol.newunimol.helpdesk.dto.MessageResponse;
import it.unimol.newunimol.helpdesk.dto.TicketHistoryResponse;
import it.unimol.newunimol.helpdesk.dto.TicketRequest;
import it.unimol.newunimol.helpdesk.dto.TicketResponse;
import it.unimol.newunimol.helpdesk.model.Messaggio;
import it.unimol.newunimol.helpdesk.model.Ticket;
import it.unimol.newunimol.helpdesk.model.TicketHistory;
import it.unimol.newunimol.helpdesk.model.User;
import it.unimol.newunimol.helpdesk.model.enums.Categoria;
import it.unimol.newunimol.helpdesk.model.enums.TicketStatus;

// qui andiamo a mappare i dati per creare i vari oggetti.
@Component
public class TicketMapper {
    
public Ticket toEntity(TicketRequest ticketRequest, User creatoDa) {
    Ticket ticket = new Ticket();
    ticket.setTitolo(ticketRequest.getTitolo());
    ticket.setDescrizione(ticketRequest.getDescrizione());
    ticket.setCategoria(Categoria.valueOf(ticketRequest.getCategoria().toUpperCase()));
    ticket.setTicketStatus(TicketStatus.PRESO_IN_CARICO);
    ticket.setCreatoDa(creatoDa);
    return ticket;
}

public TicketResponse toResponse(Ticket ticket) {
    TicketResponse response = new TicketResponse();
    response.setId(ticket.getId());
    response.setTitolo(ticket.getTitolo());
    response.setDescrizione(ticket.getDescrizione());
    response.setCategoria(ticket.getCategoria());
    response.setStatus(ticket.getTicketStatus());
    response.setCreatoDaExternalId(ticket.getCreatoDa().getExternalId());
    
    if (ticket.getAssegnatoA() != null) {
        response.setAssegnatoAExternalId(ticket.getAssegnatoA().getExternalId());
    } else {
        response.setAssegnatoAExternalId(null); // opzionale
    }

    response.setDataCreazione(ticket.getDataCreazione());
    response.setUltimaModifica(ticket.getUltimaModifica());

    return response;
}

public TicketHistoryResponse toHistoryResponse(TicketHistory history) {
    TicketHistoryResponse response = new TicketHistoryResponse();
    response.setStatoPrecedente(history.getStatoPrecedente());
    response.setStatoSuccessivo(history.getStatoSuccessivo());
    response.setModificatoDa(history.getModificatoDa());
    response.setDataModifica(history.getDataModifica());
    return response;
}

public Messaggio toEntity(MessageRequest request, Ticket ticket, String autoreId) {
    Messaggio msg = new Messaggio();
    msg.setContenuto(request.getContenuto());
    msg.setAutoreId(autoreId);
    msg.setTicket(ticket);
    return msg;
}

public MessageResponse toResponse(Messaggio msg) {
    MessageResponse response = new MessageResponse();
    response.setId(msg.getId_messaggio());
    response.setContenuto(msg.getContenuto());
    response.setAutoreId(msg.getAutoreId());
    response.setTimestamp(msg.getTimestamp());
    return response;
}

}
