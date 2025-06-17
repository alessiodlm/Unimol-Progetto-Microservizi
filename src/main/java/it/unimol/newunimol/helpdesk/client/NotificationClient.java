package it.unimol.newunimol.helpdesk.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import it.unimol.newunimol.helpdesk.dto.NotificationRequest;

@Component
public class NotificationClient {

    private final RestTemplate restTemplate;

    @Value("${comunicazioni.notifiche.base-url}")
    private String notificheBaseUrl;

    public NotificationClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void inviaNotifica(NotificationRequest request, String token) {
        String url = notificheBaseUrl + "/api/v1/notifications";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token); 

        HttpEntity<NotificationRequest> entity = new HttpEntity<>(request, headers);

        restTemplate.postForEntity(url, entity, Void.class); 
    }
}
