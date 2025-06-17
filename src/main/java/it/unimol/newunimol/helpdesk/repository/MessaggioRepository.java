package it.unimol.newunimol.helpdesk.repository;

import it.unimol.newunimol.helpdesk.model.Messaggio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessaggioRepository extends JpaRepository<Messaggio, Long> {
    List<Messaggio> findByTicket_Id(Long ticketId);
}
