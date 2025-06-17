package it.unimol.newunimol.helpdesk.security;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;

public class JwtUtils {

    private static RSAPublicKey publicKey;

    static {
        try {
            InputStream is = new ClassPathResource("publicKey.pem").getInputStream();
            String key = new BufferedReader(new InputStreamReader(is))
                .lines()
                .filter(line -> !line.startsWith("-----"))
                .collect(Collectors.joining());

            byte[] keyBytes = Base64.getDecoder().decode(key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            publicKey = (RSAPublicKey) kf.generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("Errore nella lettura della chiave pubblica", e);
        }
    }

    public static String extractPayload(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) throw new RuntimeException("Token malformato");
        return new String(Base64.getDecoder().decode(parts[1]));
    }

    public static String extractExternalUserId(String token) {
        String payload = extractPayload(token);
        JSONObject json = new JSONObject(payload);
        return json.getString("sub");
    }

    public static boolean validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw new IllegalArgumentException("Token malformato");

            String header = parts[0];
            String payload = parts[1];
            String signature = parts[2];

            String data = header + "." + payload;
            byte[] signatureBytes = Base64.getUrlDecoder().decode(signature);

            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(data.getBytes());

            return sig.verify(signatureBytes);
        } catch (Exception e) {
            return false;
        }
    }
}
