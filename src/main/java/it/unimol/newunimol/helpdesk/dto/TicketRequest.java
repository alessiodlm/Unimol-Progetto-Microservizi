package it.unimol.newunimol.helpdesk.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TicketRequest {
    private String titolo;
    private String descrizione;
    private String categoria;
    private String externalUserId;

}
