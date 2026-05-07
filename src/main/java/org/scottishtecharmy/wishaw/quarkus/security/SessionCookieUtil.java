package org.scottishtecharmy.wishaw.quarkus.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Signs and verifies the session cookie value using HMAC-SHA256.
 * Format on the wire: base64(username) + "." + base64(hmac)
 */
public final class SessionCookieUtil {

    private static final String ALGORITHM = "HmacSHA256";
    private static final String SECRET = "wishaw-ymca-esports-secret-key-32chars!";

    private SessionCookieUtil() {}

    public static String encode(String username) {
        try {
            String payload = Base64.getUrlEncoder().encodeToString(
                    username.getBytes(StandardCharsets.UTF_8));
            return payload + "." + hmac(payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode session cookie", e);
        }
    }

    /** Returns the username if the signature is valid, otherwise null. */
    public static String decode(String cookieValue) {
        try {
            int dot = cookieValue.lastIndexOf('.');
            if (dot < 0) return null;
            String payload = cookieValue.substring(0, dot);
            String sig = cookieValue.substring(dot + 1);
            if (!sig.equals(hmac(payload))) return null;
            return new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private static String hmac(String data) throws Exception {
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), ALGORITHM));
        return Base64.getUrlEncoder().encodeToString(
                mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }
}

