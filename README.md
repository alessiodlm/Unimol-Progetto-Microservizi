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
| Database               | MySQL 8                      | Memorizzazione dei ticket, utenti, messaggi e storico                |
| Documentazione API     | Swagger / Springdoc OpenAPI  | Generazione automatica e interattiva della documentazione REST      |
| Messaggistica / Eventi | RabbitMQ                     | Invio asincrono di notifiche e comunicazione con altri microservizi  |
---

## Dipendenze con Altri Microservizi

| Microservizio                    | Finalità                                               |
|----------------------------------|--------------------------------------------------------|
| Gestione Utenti e Ruoli          | Identificazione e autorizzazione tramite token e ruoli |
| Comunicazioni e Notifiche        | Invio notifiche su aggiornamenti ticket                 |
| Gestione Biblioteca / Tasse / Esami / etc. | Le categorie dei ticket si basano su questi moduli         |

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

### D. `NOTIFICA`

| Campo          | Descrizione                                     |
|----------------|-------------------------------------------------|
| id             | ID notifica (PK)                                |
| utente_id      | ID destinatario (FK → User)                     |
| messaggio      | Testo della notifica                            |
| tipo           | ENUM: TICKET_APERTO, RISPOSTA_OPERATORE, ecc.  |
| data_invio     | Timestamp                                       |
| letto          | BOOLEAN per indicare se la notifica è stata letta |

---

### E. `TicketHistory`

| Campo            | Descrizione                                         |
|------------------|-----------------------------------------------------|
| id               | Identificatore univoco della modifica              |
| ticket_id        | Riferimento al ticket associato (FK → Ticket)      |
| changed_by       | ID dell’utente che ha effettuato la modifica       |
| old_status       | Stato precedente del ticket                        |
| new_status       | Nuovo stato del ticket                             |
| change_date      | Data e ora della modifica                          |
| comment          | Commento associato alla modifica                   |

---
## Documentazione API 

---

### Gestione Ticket  
**Obiettivo**: Gestire i ticket creati da studenti e docenti e gestiti da operatori.

```java
########## 1. Creazione nuovo Ticket ##########
# @func : createTicket()
# @param: TicketRequest ticketRequest 
# @return: ResponseEntity<TicketResponse>
POST /api/v1/tickets

########## 2. Elenco dei ticket dell'utente loggato ##########
# @func: getMyTickets()
# @param: String externalUserId
# @return: ResponseEntity<List<TicketResponse>>
GET /api/v1/tickets

########## 3. Dettagli di un singolo ticket ##########
# @func: getTicketById()
# @param: Long id
# @return: ResponseEntity<TicketResponse>
GET /api/v1/tickets/{id}

########## 4. Assegnazione ticket a un operatore ##########
# @func: assignTicketToOperator()
# @param: Long ticketId, String operatorId
# @return: ResponseEntity<Void>
PUT /api/v1/tickets/{ticketId}/assegna

########## 5. Cambio stato del ticket ##########
# @func: updateTicketStatus()
# @param: Long ticketId, TicketStatusUpdateRequest request
# @return: ResponseEntity<Void>
PUT /api/v1/tickets/{ticketId}/stato

########## 6. Cronologia modifiche del ticket ##########
# @func: getTicketHistory()
# @param: Long ticketId
# @return: ResponseEntity<List<TicketHistoryResponse>>
GET /api/v1/tickets/{ticketId}/storico
```

### Gestione Messaggi  
**Obiettivo**: Permettere la comunicazione tra utenti e operatori all'interno dei ticket.

```java
########## 1. Invio nuovo messaggio all'interno del ticket ##########
# @func: sendMessage()
# @param: MessageRequest messageRequest
# @return: ResponseEntity<MessageResponse>
POST /api/v1/tickets/{ticketId}/messaggi

########## 2. Elenco dei messaggi di un ticket ##########
# @func: getMessagesByTicket()
# @param: Long ticketId
# @return: ResponseEntity<List<MessageResponse>>
GET /api/v1/tickets/{ticketId}/messaggi
```

### Gestione Notifiche 
**Obiettivo**: Gestire la notificazione di stati e messaggi relativi ai ticket.
```java
########## 1. Elenco notifiche per l'utente autenticato ##########
# @func: getUserNotifications()
# @param: String externalUserId
# @return: ResponseEntity<List<NotificationResponse>>
GET /api/v1/notifiche

########## 2. Segna una notifica come letta ##########
# @func: markNotificationAsRead()
# @param: Long notificationId
# @return: ResponseEntity<Void>
PUT /api/v1/notifiche/{notificationId}/lettura

########## 3. Crea una nuova notifica ##########
# @func: createNotification()
# @param: NotificationRequest notificationRequest
# @return: ResponseEntity<NotificationResponse>
POST /api/v1/notifiche
```

### Storico Ticket
**Obiettivo**: Tracciare i cambiamenti di stato dei ticket
```java
########## 1. Ottenere lo storico delle modifiche di un ticket ##########
# @func: getTicketHistory()
# @param: Long ticketId
# @return: ResponseEntity<List<TicketHistoryResponse>>
GET /api/v1/tickets/{ticketId}/storico

########## 2. Api interna che aggiorna lo stato delle richieste ##########
# @func: addTicketHistory()
# @param: TicketHistoryRequest historyRequest
# @return: ResponseEntity<TicketHistoryResponse>
POST /api/v1/tickets/{ticketId}/storico
```
