package it.unimol.newunimol.helpdesk.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unimol.newunimol.helpdesk.dto.MessageRequest;
import it.unimol.newunimol.helpdesk.dto.MessageResponse;
import it.unimol.newunimol.helpdesk.dto.TicketHistoryResponse;
import it.unimol.newunimol.helpdesk.dto.TicketRequest;
import it.unimol.newunimol.helpdesk.dto.TicketResponse;
import it.unimol.newunimol.helpdesk.dto.TicketStatusUpdateRequest;
import it.unimol.newunimol.helpdesk.service.MessageService;
import it.unimol.newunimol.helpdesk.service.TicketService;


@Tag(name = "Ticket", description = "API per la gestione dei ticket dell'help desk")
@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final MessageService messageService;

    public TicketController(TicketService ticketService, MessageService messageService) {
        this.ticketService = ticketService;
        this.messageService = messageService;
    }


     @Operation(summary = "Crea un nuovo ticket", description = "Permette all'utente autenticato di creare un ticket")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ticket creato con successo"),
        @ApiResponse(responseCode = "400", description = "Dati non validi"),
        @ApiResponse(responseCode = "401", description = "Token mancante o non valido")
    })
    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @RequestBody TicketRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        TicketResponse createdTicket = ticketService.createTicket(request, token);
        return ResponseEntity.ok(createdTicket);
    }


    @Operation(summary = "Recupera tutti i ticket dell'utente", description = "Restituisce la lista dei ticket creati dall'utente autenticato")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista restituita con successo"),
        @ApiResponse(responseCode = "401", description = "Token mancante o non valido")
    })
    @GetMapping
    public ResponseEntity<List<TicketResponse>> getMyTickets(
        @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        return ResponseEntity.ok(ticketService.getMyTickets(token));
    }


    @Operation(summary = "Recupera un ticket per ID", description = "Restituisce i dettagli di un ticket se appartiene all'utente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ticket trovato"),
        @ApiResponse(responseCode = "404", description = "Ticket non trovato"),
        @ApiResponse(responseCode = "403", description = "Accesso non autorizzato al ticket"),
        @ApiResponse(responseCode = "401", description = "Token mancante o non valido")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(
        @PathVariable Long id,
        @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        return ResponseEntity.ok(ticketService.getTicketById(id, token));
    }


        @Operation(summary = "Assegna un operatore a un ticket", description = "Permette agli operatori di assegnarsi un ticket non ancora assegnato")
        @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operatore assegnato"),
        @ApiResponse(responseCode = "403", description = "Utente non autorizzato"),
        @ApiResponse(responseCode = "404", description = "Ticket non trovato"),
        @ApiResponse(responseCode = "401", description = "Token mancante o non valido")
    })
    @PutMapping("/{ticketId}/assegna")
    public ResponseEntity<TicketResponse> assegnaOperatore(
        @PathVariable Long ticketId,
        @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        return ResponseEntity.ok(ticketService.assegnaOperatore(ticketId, token));
    }


        @Operation(summary = "Aggiorna lo stato di un ticket", description = "Permette all'operatore assegnato di aggiornare lo stato del ticket")
        @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Stato aggiornato"),
        @ApiResponse(responseCode = "400", description = "Stato non valido"),
        @ApiResponse(responseCode = "403", description = "Utente non autorizzato"),
        @ApiResponse(responseCode = "404", description = "Ticket non trovato"),
        @ApiResponse(responseCode = "401", description = "Token mancante o non valido")
    })
    @PutMapping("/{ticketId}/stato")
    public ResponseEntity<Void> updateTicketStatus(
        @PathVariable Long ticketId,
        @RequestBody TicketStatusUpdateRequest request,
        @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        ticketService.updateTicketStatus(ticketId, request, token);
        return ResponseEntity.ok().build();
    }


@Operation(
    summary = "Invia un messaggio per un ticket",
    description = "Permette a un utente (creatore o operatore assegnato) di inviare un messaggio all'interno del ticket identificato da ID"
)
@ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Messaggio creato con successo"),
    @ApiResponse(responseCode = "400", description = "Contenuto del messaggio mancante o non valido"),
    @ApiResponse(responseCode = "403", description = "Utente non autorizzato a scrivere su questo ticket"),
    @ApiResponse(responseCode = "404", description = "Ticket non trovato"),
    @ApiResponse(responseCode = "401", description = "Token mancante o non valido")
})
@PostMapping("/{ticketId}/messaggi")
public ResponseEntity<MessageResponse> sendMessage(
    @PathVariable Long ticketId,
    @RequestBody MessageRequest request,
    @RequestHeader("Authorization") String authHeader) {

    String token = authHeader.replace("Bearer ", "");
    MessageResponse response = messageService.sendMessage(ticketId, request, token);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}


@Operation(
    summary = "Recupera i messaggi di un ticket",
    description = "Restituisce la lista di tutti i messaggi associati al ticket identificato da ID. Accesso consentito solo al creatore o all'operatore assegnato."
)
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Messaggi recuperati con successo"),
    @ApiResponse(responseCode = "403", description = "Utente non autorizzato a visualizzare i messaggi di questo ticket"),
    @ApiResponse(responseCode = "404", description = "Ticket non trovato"),
    @ApiResponse(responseCode = "401", description = "Token mancante o non valido")
})
@GetMapping("/{ticketId}/messaggi")
public ResponseEntity<List<MessageResponse>> getMessagesByTicket(
    @PathVariable Long ticketId,
    @RequestHeader("Authorization") String authHeader) {

    String token = authHeader.replace("Bearer ", "");
    List<MessageResponse> messaggi = messageService.getMessagesByTicket(ticketId, token);
    return ResponseEntity.ok(messaggi);
}


 @Operation(summary = "Recupera lo storico delle modifiche di un ticket", description = "Restituisce la cronologia delle modifiche di stato del ticket")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Storico restituito con successo"),
        @ApiResponse(responseCode = "404", description = "Ticket non trovato"),
        @ApiResponse(responseCode = "401", description = "Token mancante o non valido")
    })
    @GetMapping("/{ticketId}/storico")
    public ResponseEntity<List<TicketHistoryResponse>> getTicketHistory(
        @PathVariable Long ticketId,
        @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        return ResponseEntity.ok(ticketService.getTicketHistory(ticketId, token));
    }
}
