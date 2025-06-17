package it.unimol.newunimol.helpdesk.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import it.unimol.newunimol.helpdesk.client.UserClient;
import it.unimol.newunimol.helpdesk.dto.MessageRequest;
import it.unimol.newunimol.helpdesk.dto.MessageResponse;
import it.unimol.newunimol.helpdesk.dto.TicketHistoryRequest;
import it.unimol.newunimol.helpdesk.dto.TicketHistoryResponse;
import it.unimol.newunimol.helpdesk.dto.TicketRequest;
import it.unimol.newunimol.helpdesk.dto.TicketResponse;
import it.unimol.newunimol.helpdesk.dto.TicketStatusUpdateRequest;
import it.unimol.newunimol.helpdesk.dto.UserProfileResponse;
import it.unimol.newunimol.helpdesk.mapper.TicketHistoryMapper;
import it.unimol.newunimol.helpdesk.mapper.TicketMapper;
import it.unimol.newunimol.helpdesk.model.Ticket;
import it.unimol.newunimol.helpdesk.model.TicketHistory;
import it.unimol.newunimol.helpdesk.model.User;
import it.unimol.newunimol.helpdesk.model.enums.Ruolo;
import it.unimol.newunimol.helpdesk.model.enums.TicketStatus;
import it.unimol.newunimol.helpdesk.repository.TicketHistoryRepository;
import it.unimol.newunimol.helpdesk.repository.TicketRepository;
import it.unimol.newunimol.helpdesk.repository.UserRepository;
import it.unimol.newunimol.helpdesk.security.JwtUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final UserClient userClient;
    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final TicketHistoryRepository ticketHistoryRepository;
    private final UserRepository userRepository;

    @Autowired
    private final NotificationService notificationService;
    private final TicketHistoryMapper ticketHistoryMapper;

    @Autowired
    private MessageService messageService;

    

public TicketResponse createTicket(TicketRequest request, String token) {
    if (!JwtUtils.validateToken(token)) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token non valido");
    }

    UserProfileResponse userProfile = userClient.getUserProfile(token);

    User creatoDa = userRepository.findByExternalId(userProfile.getIdUtente())
        .orElseGet(() -> {
            User nuovoUtente = new User();
            nuovoUtente.setExternalId(userProfile.getIdUtente());
            nuovoUtente.setNome(userProfile.getNome());
            return userRepository.save(nuovoUtente);
        });

    Ticket ticket = ticketMapper.toEntity(request, creatoDa);
    Ticket salvato = ticketRepository.save(ticket);

    return ticketMapper.toResponse(salvato);
}

    public List<TicketResponse> getMyTickets(String token) {
        if (!JwtUtils.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token non valido");
        }

        String externalUserId = JwtUtils.extractExternalUserId(token);
        List<Ticket> tickets = ticketRepository.findByCreatoDa_ExternalId(externalUserId);

        return tickets.stream().map(ticketMapper::toResponse).toList();
    }

    public TicketResponse getTicketById(Long id, String token) {
        if (!JwtUtils.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token non valido");
        }

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket non trovato"));

        return ticketMapper.toResponse(ticket);
    }

    public TicketResponse assegnaOperatore(Long ticketId, String token) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket non trovato"));

        if (ticket.getAssegnatoA() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket già assegnato");
        }

        UserProfileResponse utenteLoggato = userClient.getUserProfile(token);

        if (!"OPERATORE".equalsIgnoreCase(utenteLoggato.getRuolo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Utente non autorizzato");
        }
        User operatore = new User();
        operatore.setExternalId(utenteLoggato.getIdUtente());
        operatore.setNome(utenteLoggato.getNome());
        operatore.setRuolo(Ruolo.OPERATORE);
        ticket.setAssegnatoA(operatore);
        ticket.setTicketStatus(TicketStatus.IN_LAVORAZIONE);
        ticketRepository.save(ticket);

        notificationService.inviaNotificaAssegnazione(ticket, token);

        return ticketMapper.toResponse(ticket);
    }


    public void updateTicketStatus(Long ticketId, TicketStatusUpdateRequest request, String token) {
        if (!JwtUtils.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token non valido");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket non trovato"));

        UserProfileResponse user = userClient.getUserProfile(token);

        if (!"OPERATORE".equalsIgnoreCase(user.getRuolo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo gli operatori possono cambiare stato");
        }

        TicketStatus nuovoStato;
        try {
            nuovoStato = TicketStatus.valueOf(request.getNuovoStato());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stato non valido: " + request.getNuovoStato());
        }

        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setStatoPrecedente(ticket.getTicketStatus().name());
        history.setStatoSuccessivo(nuovoStato.name());
        history.setModificatoDa(user.getIdUtente());
        ticketHistoryRepository.save(history);

        ticket.setTicketStatus(nuovoStato);
        ticketRepository.save(ticket);

        String autoreId = user.getIdUtente();
        notificationService.inviaNotificaCambioStato(ticket, autoreId, token);
    }


    public List<TicketHistoryResponse> getTicketHistory(Long ticketId, String token) {
        if (!JwtUtils.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token non valido");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket non trovato"));

        List<TicketHistory> storico = ticketHistoryRepository.findByTicket_Id(ticketId);

        return storico.stream().map(ticketHistoryMapper::toHistoryResponse).toList();
    }


    public TicketHistoryResponse addTicketHistory(TicketHistoryRequest request, String token) {
        if (!JwtUtils.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token non valido");
        }

        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket non trovato"));

        TicketHistory history = TicketHistoryMapper.toEntity(request, ticket);
        ticketHistoryRepository.save(history);

        return ticketHistoryMapper.toHistoryResponse(history);
    }

    
public MessageResponse inviaMessaggioTramiteService(Long ticketId, MessageRequest request, String token) {
    return messageService.sendMessage(ticketId, request, token);
}


   
}