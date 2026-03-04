package upf.at.ban.service;


import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import upf.at.ban.util.Constants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ValidateNameService {

    // Logger per aquesta classe
    private static final Logger logger = LogManager.getLogger(ValidateNameService.class);
    
    // ------------- PUBLIC (lo que llamas desde ClientResource) -------------

    public boolean isNameMatchingPhone(String phone, String name) {

        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("phone is required");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name is required");
        }

        String loginHint = toLoginHint(phone);  // "tel:+34..."
        String e164 = toE164(phone);            // "+34..."

        logger.info("ValidateName_MATCH_START phone={} name={}", phone, name);

        Client client = ClientBuilder.newClient();
        try {
            String basicAuth = basicAuthHeader(Constants.AV_CLIENT_ID, Constants.AV_CLIENT_SECRET);

            // 1) CIBA authorize -> auth_req_id
            String authReqId = bcAuthorize(client, basicAuth, loginHint);

            // 2) token -> access_token
            String accessToken = token(client, basicAuth, authReqId);

            // 3) match -> nameMatch
            boolean ok = matchName(client, accessToken, e164, name.trim());

            logger.info("ValidateName phone={} ok={}", phone, ok);
            return ok;

        } finally {
            client.close();
        }
    }
    
        
    

    // ------------- CURL 1: POST /bc-authorize -------------

    private String bcAuthorize(Client client, String basicAuth, String loginHint) {
        WebTarget target = client.target(Constants.CIBA_AUTHORIZE_PATH); // .../bc-authorize

        Form form = new Form()
                .param("login_hint", loginHint)
                .param("scope", Constants.AV_SCOPE); // Know your costumer scope

        Response r = target.request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", basicAuth)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        if (r.getStatus() / 100 != 2) {
            throw new RuntimeException("bc-authorize HTTP " + r.getStatus() + ": " + safeBody(r));
        }

        // Respuesta JSON: { "auth_req_id": "..." , ... }
        Map<String, Object> json = r.readEntity(new GenericType<Map<String, Object>>() {});
        Object v = json.get("auth_req_id");
        if (v == null) throw new RuntimeException("bc-authorize: missing auth_req_id");
        return v.toString();
    }

    // ------------- CURL 2: POST /token -------------

    private String token(Client client, String basicAuth, String authReqId) {
        WebTarget target = client.target(Constants.AV_TOKEN_URL); // .../token

        Form form = new Form()
                .param("grant_type", Constants.AV_GRANT_TYPE) // urn:openid:params:grant-type:ciba
                .param("auth_req_id", authReqId);

        Response r = target.request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", basicAuth)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        if (r.getStatus() / 100 != 2) {
            throw new RuntimeException("token HTTP " + r.getStatus() + ": " + safeBody(r));
        }

        // Respuesta JSON: { "access_token": "...", ... }
        Map<String, Object> json = r.readEntity(new GenericType<Map<String, Object>>() {});
        Object v = json.get("access_token");
        if (v == null) throw new RuntimeException("token: missing access_token");
        return v.toString();
    }

    // ------------- CURL 3: POST /verify -------------

    private boolean matchName(Client client, String accessToken, String phoneE164, String name) {
        WebTarget target = client.target(Constants.AV_VERIFY_URL); // .../kyc-age-verification/v0.1/verify

        // Request
       String jsonBody = "{"
                + "\"phoneNumber\":\"" + jsonEscape(phoneE164) + "\","
                + "\"name\":\"" + jsonEscape(name) + "\""
                + "}";

        Response r = target.request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer " + accessToken)
                .post(Entity.entity(jsonBody, MediaType.APPLICATION_JSON_TYPE));

        if (r.getStatus() / 100 != 2) {
            throw new RuntimeException("verify HTTP " + r.getStatus() + ": " + safeBody(r));
        }

        // Respuesta JSON: 
        Map<String, Object> json = r.readEntity(new GenericType<Map<String, Object>>() {});
        Object v = json.get("nameMatch"); // "true" / "false" / "not_available"
        if (v == null) return false;

        if (v instanceof Boolean) return (Boolean) v;

        String s = String.valueOf(v).trim();
        return "true".equalsIgnoreCase(s);

    }

    // ------------- HELPERS -------------

    // Revisa que el telefon estigui bé formatat i el converteix a "tel:+34666..." per passar-lo com login_hint
    private static String toLoginHint(String phone) { 

        String p = phone.trim();
        if (p.startsWith("tel:")) return p;

        // quita separadores, deja solo dígitos y +
        p = p.replaceAll("[^0-9+]", "");

        // si viene "666666666" asumimos ES (+34)
        if (p.matches("\\d{9}")) p = "+34" + p;
        else if (p.matches("\\d+")) p = "+" + p;

        return "tel:" + p;
    }

    // Igual que toLoginHint però sense tel:
    private static String toE164(String phone) {
        String p = phone.trim();
        if (p.startsWith("tel:")) p = p.substring(4);
        p = p.replaceAll("[^0-9+]", "");
        if (p.matches("\\d{9}")) p = "+34" + p;
        else if (p.matches("\\d+")) p = "+" + p;
        return p;
    }

    // Crea el header de autenticación Basic a partir del clientId y clientSecret
    private static String basicAuthHeader(String clientId, String clientSecret) {
        String raw = clientId + ":" + clientSecret;
        String b64 = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        return "Basic " + b64;
    }

    // Lee el body de la respuesta de forma segura (si no es puede leer, devuelve "<no body>")
    private static String safeBody(Response r) {
        try { return r.readEntity(String.class); }
        catch (Exception e) { return "<no body>"; }
    }

    // Verificar que està ben construit 
    private static String jsonEscape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    
}
