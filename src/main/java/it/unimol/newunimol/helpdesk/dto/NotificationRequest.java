package it.unimol.newunimol.helpdesk.dto;

import lombok.Data;

@Data
public class NotificationRequest {
    private String userId;        
    private String type; 
    private String title;         
    private String message;       
    private Long referenceId;     
    private String link;          
}
