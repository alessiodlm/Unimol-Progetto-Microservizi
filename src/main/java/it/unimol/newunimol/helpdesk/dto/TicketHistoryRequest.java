
package it.unimol.newunimol.helpdesk.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TicketHistoryRequest {
    private String statoPrecedente;
    private String statoAttuale;
    private String modificatoDaId;
    private Long ticketId;
}
