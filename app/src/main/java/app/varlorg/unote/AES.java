package app.varlorg.unote;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {

    private static final String SALT = "app.varlorg.unote";
    private static final int ITERATION_COUNT = 1024;
    private static final int KEY_LENGTH = 256; // For AES-256

    public static SecretKey getKeyFromPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), SALT.getBytes(), ITERATION_COUNT, KEY_LENGTH);
        SecretKey secret = factory.generateSecret(spec);
        return new SecretKeySpec(secret.getEncoded(), "AES");
    }
    public static String encrypt(String plaintext, String password) throws Exception {
        SecretKey key = getKeyFromPassword(password);
        byte[] iv = new byte[12]; // For GCM, 12 bytes (96 bits) is recommended
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv); // 128-bit authentication tag length
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        // Combine IV and ciphertext for storage/transmission
        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decrypt(String combinedCiphertext, String password) throws Exception {
        SecretKey key = getKeyFromPassword(password);
        byte[] combined = Base64.getDecoder().decode(combinedCiphertext);
        byte[] iv = new byte[12];
        byte[] ciphertext = new byte[combined.length - iv.length];
        System.arraycopy(combined, 0, iv, 0, iv.length);
        System.arraycopy(combined, iv.length, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        byte[] decrypted = cipher.doFinal(ciphertext);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}