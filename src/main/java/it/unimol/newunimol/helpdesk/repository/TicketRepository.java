package it.unimol.newunimol.helpdesk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.unimol.newunimol.helpdesk.model.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
     List<Ticket> findByCreatoDa_ExternalId(String externalId);

}
