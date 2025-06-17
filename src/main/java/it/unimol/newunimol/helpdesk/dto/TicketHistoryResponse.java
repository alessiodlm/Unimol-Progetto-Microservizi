package it.unimol.newunimol.helpdesk.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TicketHistoryResponse {
    private String statoPrecedente;
    private String statoSuccessivo;
    private String modificatoDa;
    private LocalDateTime dataModifica;
}
