package com.cloudshare.cloudsharefiles.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.URL;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.security.PublicKey;
import java.util.Base64;

@Component
public class ClerkJwksProvider {

    @Value("${clerk.jwks-url}")
    private String jwksUrl;
    
    private final Map<String, PublicKey> keyCache = new HashMap<>();
    private long lastFetchTime = 0;
    private static final long CACHE_TTL = 360000; // 6 minutes

    public PublicKey getPublicKey(String kid) throws Exception {
        // Fixed the cache condition - was checking < 0 instead of < CACHE_TTL
        if (keyCache.containsKey(kid) && (System.currentTimeMillis() - lastFetchTime) < CACHE_TTL) {
            System.out.println("[DEBUG] Returning cached key for kid: " + kid);
            return keyCache.get(kid);
        }
        
        System.out.println("[DEBUG] Cache miss or expired, refreshing keys for kid: " + kid);
        refreshKeys();
        return keyCache.get(kid);
    }

    public void refreshKeys() throws Exception {
        System.out.println("[DEBUG] Refreshing keys from: " + jwksUrl);
        
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jwks = mapper.readTree(new URL(jwksUrl));
        
        System.out.println("[DEBUG] JWKS response: " + jwks.toString());
        
        JsonNode keys = jwks.get("keys");
        if (keys == null || !keys.isArray()) {
            throw new Exception("Invalid JWKS format: missing or invalid 'keys' array");
        }
        
        keyCache.clear(); // Clear old keys
        
        for (JsonNode keyNode : keys) {
            try {
                // Fixed: Remove the extra space in " kid" -> "kid"
                JsonNode kidNode = keyNode.get("kid");
                JsonNode ktyNode = keyNode.get("kty");
                JsonNode algNode = keyNode.get("alg");
                JsonNode nNode = keyNode.get("n");
                JsonNode eNode = keyNode.get("e");
                
                // Add null checks before calling asText()
                if (kidNode == null) {
                    System.out.println("[DEBUG] Skipping key with missing 'kid'");
                    continue;
                }
                
                if (ktyNode == null) {
                    System.out.println("[DEBUG] Skipping key with missing 'kty' for kid: " + kidNode.asText());
                    continue;
                }
                
                String kid = kidNode.asText();
                String kty = ktyNode.asText();
                
                // Fixed: RS256 not RSA256
                String alg = (algNode != null) ? algNode.asText() : "";
                
                System.out.println("[DEBUG] Processing key - kid: " + kid + ", kty: " + kty + ", alg: " + alg);
                
                if ("RSA".equals(kty)) { // Remove algorithm check as it might not always be present
                    if (nNode == null || eNode == null) {
                        System.out.println("[DEBUG] Skipping RSA key with missing 'n' or 'e' for kid: " + kid);
                        continue;
                    }
                    
                    String n = nNode.asText();
                    String e = eNode.asText();
                    
                    PublicKey publicKey = createPublicKey(n, e);
                    keyCache.put(kid, publicKey);
                    System.out.println("[DEBUG] Successfully cached key for kid: " + kid);
                }
                
            } catch (Exception e) {
                System.out.println("[DEBUG] Error processing individual key: " + e.getMessage());
                e.printStackTrace();
                // Continue with other keys
            }
        }
        
        lastFetchTime = System.currentTimeMillis();
        System.out.println("[DEBUG] Refresh complete. Cached " + keyCache.size() + " keys");
        System.out.println("[DEBUG] Available kids: " + keyCache.keySet());
    }

    private PublicKey createPublicKey(String modulus, String exponent) throws Exception {
        try {
            byte[] modulusBytes = Base64.getUrlDecoder().decode(modulus);
            byte[] exponentBytes = Base64.getUrlDecoder().decode(exponent);

            BigInteger modulusBigInt = new BigInteger(1, modulusBytes);
            BigInteger exponentBigInt = new BigInteger(1, exponentBytes);

            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulusBigInt, exponentBigInt);
            KeyFactory factory = KeyFactory.getInstance("RSA");

            return factory.generatePublic(spec);
        } catch (Exception e) {
            System.out.println("[DEBUG] Failed to create public key from modulus/exponent: " + e.getMessage());
            throw e;
        }
    }
}