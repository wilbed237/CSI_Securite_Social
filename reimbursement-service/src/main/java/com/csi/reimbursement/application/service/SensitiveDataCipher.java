package com.csi.reimbursement.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class SensitiveDataCipher {
    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();
    public SensitiveDataCipher(@Value("${security.sensitive-data-key}") String secret) {
        try { key = new SecretKeySpec(Arrays.copyOf(MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8)), 32), "AES"); }
        catch (Exception ex) { throw new IllegalStateException("Impossible d'initialiser le chiffrement", ex); }
    }
    public String encrypt(String value) {
        if (value == null || value.isBlank()) return null;
        try { byte[] iv = new byte[12]; random.nextBytes(iv); Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv)); byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            byte[] payload = new byte[iv.length + encrypted.length]; System.arraycopy(iv, 0, payload, 0, iv.length); System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(payload); } catch (Exception ex) { throw new IllegalStateException("Echec du chiffrement", ex); }
    }
    public String decrypt(String value) {
        if (value == null || value.isBlank()) return null;
        try { byte[] payload = Base64.getDecoder().decode(value); byte[] iv = Arrays.copyOfRange(payload, 0, 12); byte[] encrypted = Arrays.copyOfRange(payload, 12, payload.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8); } catch (Exception ex) { throw new IllegalStateException("Echec du dechiffrement", ex); }
    }
}
