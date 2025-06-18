# Unimol: Microservizio Supporto & Help Desk
Documentazione progetto Microservizi: sviluppo sezione "Supporto &amp; Help Desk"

# Progetto Microservizi: Servizio Supporto e Help Desk

## Overview

Il microservizio è responsabile della gestione delle richieste di supporto da parte di **Docenti** e **Studenti**, nonché delle attività amministrative correlate.

### Funzionalità principali:
- **Studenti e Docenti**:  
  - Apertura di ticket di supporto in base a un'area tematica (es: tasse, biblioteca)
  - Interazione con gli operatori incaricati alla risoluzione

- **Amministrativi**:  
  - Gestione dei ticket attraverso diversi stati:
    - Aperto
    - Preso in carico
    - In lavorazione
    - Informazioni necessarie
    - Risolto
    - Chiuso
  - Visualizzazione dello storico e tracciamento delle modifiche apportate ai ticket

---

## Architettura del Microservizio

| Componente             | Tecnologia / Framework       | Ruolo                                                                 |
|------------------------|------------------------------|----------------------------------------------------------------------|
| Framework Principale   | Spring Boot                  | Gestione REST API, configurazione, logica applicativa               |
| Database               | Postgresql                      | Memorizzazione dei ticket, utenti, messaggi e storico                |
| Documentazione API     | Swagger / Springdoc OpenAPI  | Generazione automatica e interattiva della documentazione REST      |
| Messaggistica / Eventi | RabbitMQ                     | Invio asincrono di notifiche e comunicazione con altri microservizi  |
---

## Dipendenze con Altri Microservizi

| Microservizio                    | Finalità                                               |
|----------------------------------|--------------------------------------------------------|
| Gestione Utenti e Ruoli          | Identificazione e autorizzazione tramite token e ruoli |
| Comunicazioni e Notifiche        | Invio notifiche su aggiornamenti ticket                 |

---

## Struttura delle Entità

### A. `USER`

| Campo        | Descrizione                              |
|-------------|------------------------------------------|
| id           | ID interno dell’utente (PK)              |
| external_id  | ID esterno (collegamento ad auth service)|
| nome         | Nome dell’utente                         |
| ruolo        | ENUM: STUDENTE, DOCENTE, OPERATORE       |

---

### B. `TICKET`

| Campo            | Descrizione                                      |
|------------------|--------------------------------------------------|
| id               | ID del ticket (PK)                               |
| titolo           | Titolo sintetico del problema                    |
| descrizione      | Descrizione dettagliata                          |
| stato            | ENUM: APERTO, PRESO_IN_CARICO, IN_LAVORAZIONE, IN_ATTESA, RISOLTO, CHIUSO |
| categoria        | Categoria del problema (es: LOGIN, CARRIERA, PAGAMENTI, ecc.) |
| creato_da_id     | ID dell'utente che ha creato il ticket (FK → User) |
| assegnato_a_id   | ID dell’operatore assegnato (FK → User, nullable) |
| data_creazione   | Timestamp di creazione                           |
| data_aggiornamento | Timestamp dell’ultimo aggiornamento              |

---

### C. `MESSAGGIO`

| Campo           | Descrizione                                      |
|------------------|--------------------------------------------------|
| id               | ID messaggio (PK)                                |
| ticket_id        | ID del ticket a cui è collegato (FK → Ticket)    |
| autore_id        | ID dell’utente che ha inviato il messaggio (FK → User) |
| contenuto        | Testo del messaggio                              |
| data_invio       | Timestamp dell’invio                             |
| allegato_url     | URL opzionale di un file allegato (es. S3)       |

---

### D. `TicketHistory`

| Campo            | Descrizione                                         |
|------------------|-----------------------------------------------------|
| id               | Identificatore univoco della modifica              |
| ticket_id        | Riferimento al ticket associato (FK → Ticket)      |
| modificatoDa       | ID dell’utente che ha effettuato la modifica       |
| StatoPrecedente     | Stato precedente del ticket                        |
| StatoSuccessivo       | Nuovo stato del ticket                             |
| dataModifica      | Data e ora della modifica                          |
| comment          | Commento associato alla modifica                   |

#  API - Microservizio Help Desk

Il microservizio gestisce la creazione e la gestione di ticket di supporto, la messaggistica tra utenti, e l'invio di notifiche tramite un microservizio esterno. Tiene anche traccia della cronologia delle modifiche dei ticket.

---

## Gestione Ticket

### 1. Creazione nuovo Ticket
- **@func**: `createTicket()`
- **@param**: `TicketRequest ticketRequest`
- **@return**: `ResponseEntity<TicketResponse>`
- **Metodo**: `POST`
- **Endpoint**: `/api/v1/tickets`

---

### 2. Elenco dei ticket dell'utente loggato
- **@func**: `getMyTickets()`
- **@param**: `String externalUserId` *(derivato dal token)*
- **@return**: `ResponseEntity<List<TicketResponse>>`
- **Metodo**: `GET`
- **Endpoint**: `/api/v1/tickets`

---

### 3. Dettagli di un singolo ticket
- **@func**: `getTicketById()`
- **@param**: `Long id`
- **@return**: `ResponseEntity<TicketResponse>`
- **Metodo**: `GET`
- **Endpoint**: `/api/v1/tickets/{id}`

---

### 4. Assegnazione ticket a un operatore
- **@func**: `assignTicketToOperator()`
- **@param**: `Long ticketId, String operatorId`
- **@return**: `ResponseEntity<Void>`
- **Metodo**: `PUT`
- **Endpoint**: `/api/v1/tickets/{ticketId}/assegna`

---

### 5. Cambio stato del ticket
- **@func**: `updateTicketStatus()`
- **@param**: `Long ticketId, TicketStatusUpdateRequest request`
- **@return**: `ResponseEntity<Void>`
- **Metodo**: `PUT`
- **Endpoint**: `/api/v1/tickets/{ticketId}/stato`

---

### 6. Cronologia modifiche del ticket
- **@func**: `getTicketHistory()`
- **@param**: `Long ticketId`
- **@return**: `ResponseEntity<List<TicketHistoryResponse>>`
- **Metodo**: `GET`
- **Endpoint**: `/api/v1/tickets/{ticketId}/storico`

---

## Messaggistica

### 1. Invio nuovo messaggio all'interno del ticket
- **@func**: `sendMessage()`
- **@param**: `MessageRequest messageRequest`
- **@return**: `ResponseEntity<MessageResponse>`
- **Metodo**: `POST`
- **Endpoint**: `/api/v1/tickets/{ticketId}/messaggi`

---

### 2. Elenco dei messaggi di un ticket
- **@func**: `getMessagesByTicket()`
- **@param**: `Long ticketId`
- **@return**: `ResponseEntity<List<MessageResponse>>`
- **Metodo**: `GET`
- **Endpoint**: `/api/v1/tickets/{ticketId}/messaggi`

---

## Notifiche (invio tramite client verso microservizio Comunicazioni)

### 1. Crea una nuova notifica
- **@func**: `createNotification()`
- **@param**: `NotificationRequest notificationRequest`
- **@return**: `ResponseEntity<NotificationResponse>`
- **Metodo**: `POST`
- **Endpoint**: `/api/v1/notifiche`

> ⚠️ Questa API è invocata internamente.


---

## Ticket History

### 1. Ottenere lo storico delle modifiche di un ticket
- **@func**: `getTicketHistory()`
- **@param**: `Long ticketId`
- **@return**: `ResponseEntity<List<TicketHistoryResponse>>`
- **Metodo**: `GET`
- **Endpoint**: `/api/v1/tickets/{ticketId}/storico`

---


