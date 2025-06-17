package it.unimol.newunimol.helpdesk.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MessageResponse {
    private Long id;
    private String contenuto;
    private String autoreId;
    private LocalDateTime timestamp;
}
