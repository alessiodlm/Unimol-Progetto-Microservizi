package it.unimol.newunimol.helpdesk.dto;

import java.time.LocalDateTime;

import it.unimol.newunimol.helpdesk.model.enums.Categoria;
import it.unimol.newunimol.helpdesk.model.enums.TicketStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TicketResponse {
    private Long id;
private String titolo;
private String descrizione;
private TicketStatus status;
private Categoria categoria;
private String creatoDaExternalId;
private String assegnatoAExternalId; // può essere null cioè non assegnato a nessuno
private LocalDateTime dataCreazione;
private LocalDateTime ultimaModifica;


}
