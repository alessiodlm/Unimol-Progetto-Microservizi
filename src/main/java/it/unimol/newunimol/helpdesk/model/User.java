package it.unimol.newunimol.helpdesk.model;

import it.unimol.newunimol.helpdesk.model.enums.Ruolo;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "app_user")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id; //questo è l'id interno del database
     private String externalId; //è l id esterno che viene usato anche dagli altri
     private String nome;

     @Enumerated(EnumType.STRING)
     private Ruolo ruolo;
}
