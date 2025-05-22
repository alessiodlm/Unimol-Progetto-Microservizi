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
| Contenitore / Deploy   | Docker                       | Containerizzazione per deploy semplice e scalabile                   |
| Monitoraggio           | Spring Actuator *(opzionale)*| Health check, metriche e debug                                       |
| Backup DB / Sicurezza  | AWS RDS Snapshot o `mysqldump` | Backup automatico e disaster recovery                                |

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
