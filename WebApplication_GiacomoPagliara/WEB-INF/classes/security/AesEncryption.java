package security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.SecureRandom;
import java.util.Base64;

import utils.ConfigManager;

/**
 * Classe per la crittografia e decrittografia usando l'algoritmo AES.
 * Utilizzata principalmente per la protezione dei dati nei cookie.
 * 
 * @author Giacomo Pagliara
 */
public class AesEncryption {
    // Costanti per l'algoritmo e i parametri
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_SPEC = "AES";
    private static final int IV_SIZE = 16; // 128 bit
    
    private static SecretKey secretKey;
    private static String ivString;

    /**
     * Inizializza la chiave e il vettore di inizializzazione (IV) necessari per AES.
     * Questo metodo viene chiamato automaticamente quando necessario.
     * 
     * @throws Exception Se si verifica un errore durante l'inizializzazione
     */
    private static void initialize() throws Exception {
        if (secretKey == null) {
            String aesKey = ConfigManager.getProperty("aes.key");
            if (aesKey == null || aesKey.isEmpty()) {
                throw new IllegalStateException("Chiave AES non trovata nelle configurazioni");
            }
            secretKey = new SecretKeySpec(Base64.getDecoder().decode(aesKey), KEY_SPEC);
        }
        
        if (ivString == null) {
            // Ottieni l'IV dalla configurazione o genera un nuovo IV sicuro
            ivString = ConfigManager.getProperty("aes.iv");
            if (ivString == null || ivString.isEmpty()) {
                // Se non è configurato, genera un IV casuale sicuro
                byte[] iv = new byte[IV_SIZE];
                new SecureRandom().nextBytes(iv);
                ivString = Base64.getEncoder().encodeToString(iv);
            }
        }
    }

    /**
     * Cripta un array di byte usando AES.
     * 
     * @param data Dati da criptare
     * @return Dati criptati
     * @throws Exception Se si verifica un errore durante la crittografia
     */
    public static byte[] encrypt(byte[] data) throws Exception {
        if (data == null) {
            return null;
        }
        
        initialize();
        
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, 
                new IvParameterSpec(Base64.getDecoder().decode(ivString)));
        return cipher.doFinal(data);
    }

    /**
     * Decripta un array di byte usando AES.
     * 
     * @param encryptedBytes Dati criptati
     * @return Dati decriptati
     * @throws Exception Se si verifica un errore durante la decrittografia
     */
    public static byte[] decrypt(byte[] encryptedBytes) throws Exception {
        if (encryptedBytes == null) {
            return null;
        }
        
        initialize();
        
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, 
                new IvParameterSpec(Base64.getDecoder().decode(ivString)));
        return cipher.doFinal(encryptedBytes);
    }
    
    /**
     * Cripta una stringa e la restituisce come stringa Base64.
     * 
     * @param data Stringa da criptare
     * @return Stringa criptata codificata in Base64
     * @throws Exception Se si verifica un errore durante la crittografia
     */
    public static String encryptToBase64(String data) throws Exception {
        if (data == null) {
            return null;
        }
        byte[] encrypted = encrypt(data.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }
    
    /**
     * Decripta una stringa Base64 e la restituisce come testo normale.
     * 
     * @param encryptedBase64 Stringa Base64 criptata
     * @return Stringa decriptata
     * @throws Exception Se si verifica un errore durante la decrittografia
     */
    public static String decryptFromBase64(String encryptedBase64) throws Exception {
        if (encryptedBase64 == null) {
            return null;
        }
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedBase64);
        byte[] decrypted = decrypt(decodedBytes);
        return new String(decrypted);
    }
}