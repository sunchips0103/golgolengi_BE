package com.golgolengi.global.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwksClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public Map<String, RSAPublicKey> fetchPublicKeys(String jwksUrl) {
        try {
            String response = restTemplate.getForObject(jwksUrl, String.class);
            JsonNode keys = objectMapper.readTree(response).get("keys");
            Map<String, RSAPublicKey> keyMap = new HashMap<>();
            for (JsonNode key : keys) {
                keyMap.put(key.get("kid").asText(),
                        buildRsaPublicKey(key.get("n").asText(), key.get("e").asText()));
            }
            return keyMap;
        } catch (Exception e) {
            throw new RuntimeException("JWKS 조회 실패: " + jwksUrl, e);
        }
    }

    public String extractKid(String idToken) {
        try {
            String header = new String(Base64.getUrlDecoder().decode(idToken.split("\\.")[0]));
            return objectMapper.readTree(header).get("kid").asText();
        } catch (Exception e) {
            throw new RuntimeException("id_token 헤더 파싱 실패", e);
        }
    }

    private RSAPublicKey buildRsaPublicKey(String n, String e) throws Exception {
        BigInteger modulus  = new BigInteger(1, Base64.getUrlDecoder().decode(n));
        BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(e));
        return (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new RSAPublicKeySpec(modulus, exponent));
    }
}