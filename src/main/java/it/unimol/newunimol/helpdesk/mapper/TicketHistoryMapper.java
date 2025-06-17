package it.unimol.newunimol.helpdesk.mapper;

import org.springframework.stereotype.Component;

import it.unimol.newunimol.helpdesk.dto.TicketHistoryRequest;
import it.unimol.newunimol.helpdesk.dto.TicketHistoryResponse;
import it.unimol.newunimol.helpdesk.model.Ticket;
import it.unimol.newunimol.helpdesk.model.TicketHistory;

@Component
public class TicketHistoryMapper {

    public TicketHistoryResponse toHistoryResponse(TicketHistory history) {
    TicketHistoryResponse response = new TicketHistoryResponse();
    response.setStatoPrecedente(history.getStatoPrecedente());
    response.setStatoSuccessivo(history.getStatoSuccessivo());
    response.setModificatoDa(history.getModificatoDa());
    response.setDataModifica(history.getDataModifica());
    return response;
}

public static TicketHistory toEntity(TicketHistoryRequest request, Ticket ticket) {
    TicketHistory history = new TicketHistory();
    history.setTicket(ticket);
    history.setStatoPrecedente(request.getStatoPrecedente());
    history.setStatoSuccessivo(request.getStatoAttuale());
    history.setModificatoDa(request.getModificatoDaId());
    return history;
}

    
}
