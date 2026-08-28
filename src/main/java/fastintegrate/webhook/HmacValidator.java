package fastintegrate.webhook;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Ultra-fast constant-time cryptographic HMAC signature validator for Webhooks
 * (GitHub X-Hub-Signature-256, Stripe-Signature, Slack X-Slack-Signature, Generic HMAC).
 */
public final class HmacValidator {

    private final String algorithm;
    private final byte[] secretBytes;

    public HmacValidator(String algorithm, String secret) {
        this.algorithm = Objects.requireNonNull(algorithm, "algorithm cannot be null");
        Objects.requireNonNull(secret, "secret cannot be null");
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
    }

    public static HmacValidator sha256(String secret) {
        return new HmacValidator("HmacSHA256", secret);
    }

    public static HmacValidator sha1(String secret) {
        return new HmacValidator("HmacSHA1", secret);
    }

    public static HmacValidator sha512(String secret) {
        return new HmacValidator("HmacSHA512", secret);
    }

    /**
     * Computes the HMAC hex digest for the given payload.
     */
    public String computeHex(byte[] payload) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(secretBytes, algorithm));
            byte[] hmacBytes = mac.doFinal(payload);
            return bytesToHex(hmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HMAC computation failed", e);
        }
    }

    /**
     * Computes the HMAC hex digest for the given string payload.
     */
    public String computeHex(String payload) {
        return computeHex(payload.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Validates an expected signature against the actual computed signature using constant-time comparison.
     */
    public boolean validateHex(byte[] payload, String expectedSignatureHex) {
        if (expectedSignatureHex == null || expectedSignatureHex.isBlank()) {
            return false;
        }

        String sanitized = expectedSignatureHex.trim();
        if (sanitized.startsWith("sha256=") || sanitized.startsWith("sha1=") || sanitized.startsWith("sha512=")) {
            int eqIndex = sanitized.indexOf('=');
            sanitized = sanitized.substring(eqIndex + 1);
        }

        String computed = computeHex(payload);
        return constantTimeEquals(computed.toLowerCase(), sanitized.toLowerCase());
    }

    /**
     * Constant-time comparison to prevent timing attacks.
     */
    public static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(aBytes, bBytes);
    }

    private static String bytesToHex(byte[] bytes) {
        final StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}
