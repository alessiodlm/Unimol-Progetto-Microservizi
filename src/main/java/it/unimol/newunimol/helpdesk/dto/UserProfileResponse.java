package it.unimol.newunimol.helpdesk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class UserProfileResponse {

    @JsonProperty("id")
    private String idUtente;

    @JsonProperty("nome")
    private String nome;

    @JsonProperty("cognome")
    private String cognome;

    @JsonProperty("nomeRuolo")
    private String ruolo;

    @JsonProperty("username")
    private String username;

    @JsonProperty("email")
    private String email;
}
