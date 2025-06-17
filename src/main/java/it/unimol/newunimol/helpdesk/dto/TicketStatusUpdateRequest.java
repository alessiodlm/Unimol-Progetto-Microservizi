package it.unimol.newunimol.helpdesk.dto;

import lombok.Data;

@Data
public class TicketStatusUpdateRequest {
    private String nuovoStato;
    private String motivazione; 
}
