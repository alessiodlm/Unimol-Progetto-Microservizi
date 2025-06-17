package it.unimol.newunimol.helpdesk.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.Data;

@Entity
@Data
public class TicketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    private String statoPrecedente;
    private String statoSuccessivo;

    private String modificatoDa; // ID esterno dell’utente

    private LocalDateTime dataModifica;

    @PrePersist
    public void onCreate() {
        this.dataModifica = LocalDateTime.now();
    }
}
