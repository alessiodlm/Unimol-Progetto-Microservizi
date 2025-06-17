package it.unimol.newunimol.helpdesk.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import it.unimol.newunimol.helpdesk.client.UserClient;
import it.unimol.newunimol.helpdesk.dto.MessageRequest;
import it.unimol.newunimol.helpdesk.dto.MessageResponse;
import it.unimol.newunimol.helpdesk.dto.UserProfileResponse;
import it.unimol.newunimol.helpdesk.mapper.TicketMapper;
import it.unimol.newunimol.helpdesk.model.Messaggio;
import it.unimol.newunimol.helpdesk.model.Ticket;
import it.unimol.newunimol.helpdesk.repository.MessaggioRepository;
import it.unimol.newunimol.helpdesk.repository.TicketRepository;
import it.unimol.newunimol.helpdesk.security.JwtUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final TicketRepository ticketRepository;
    private final MessaggioRepository messaggioRepository;
    private final TicketMapper ticketMapper;
    private final UserClient userClient;
    private final NotificationService notificationService;

public MessageResponse sendMessage(Long ticketId, MessageRequest request, String token) {
        if (!JwtUtils.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token non valido");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket non trovato"));

        UserProfileResponse userProfile = userClient.getUserProfile(token);
        String autoreId = userProfile.getIdUtente();

        Messaggio messaggio = ticketMapper.toEntity(request, ticket, autoreId);
        messaggioRepository.save(messaggio);

        notificationService.inviaNotificaMessaggio(ticket, autoreId, token);

        return ticketMapper.toResponse(messaggio);
    }

public List<MessageResponse> getMessagesByTicket(Long ticketId, String token) {
        if (!JwtUtils.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token non valido");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket non trovato"));

        List<Messaggio> messaggi = messaggioRepository.findByTicket_Id(ticketId);

        return messaggi
        .stream()
        .map(ticketMapper::toResponse).toList();
    }


}

