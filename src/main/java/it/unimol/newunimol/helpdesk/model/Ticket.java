package it.unimol.newunimol.helpdesk.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import it.unimol.newunimol.helpdesk.model.enums.Categoria;
import it.unimol.newunimol.helpdesk.model.enums.TicketStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;



@Entity
@Data
public class Ticket {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long Id;
    private String titolo; // è la breve descrizione del problema 
    private String descrizione; // è la descrizione dettagliata, il contenuto 

   @Enumerated(EnumType.STRING)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    private TicketStatus ticketStatus;

    @ManyToOne
    @JoinColumn(name = "creato_da_id", nullable = false)
    private User creatoDa;

    @ManyToOne
    @JoinColumn(name = "assegnato_a_id")
    private User assegnatoA;
    
    @CreationTimestamp
    private LocalDateTime dataCreazione;

    @UpdateTimestamp
    private LocalDateTime ultimaModifica;
}
