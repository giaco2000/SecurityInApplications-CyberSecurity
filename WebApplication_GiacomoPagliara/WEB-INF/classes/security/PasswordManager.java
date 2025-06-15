package security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * Classe per la gestione sicura delle password.
 * Implementa funzionalità per la generazione di salt, hashing, e validazione
 * delle password secondo criteri di sicurezza.
 * 
 * @author Giacomo Pagliara
 */
public class PasswordManager {
    
    // Costanti per i requisiti delle password
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_+=<>?.";
    private static final String HASH_ALGORITHM = "SHA-256";
    
    /**
     * Cancella in modo sicuro il contenuto di un array di byte.
     * Utilizzato per rimuovere dati sensibili dalla memoria.
     * 
     * @param sensitiveData Array di byte da cancellare
     */
    public static void clearBytes(byte[] sensitiveData) {
        if (sensitiveData != null) {
            for (int i = 0; i < sensitiveData.length; i++) {
                sensitiveData[i] = 0;
            }
        }
    }

    /**
     * Genera un array di byte casuali per l'utilizzo come salt.
     * 
     * @param saltLength Lunghezza del salt desiderata
     * @return Array di byte casuali della lunghezza specificata
     */
    public static byte[] generateRandomBytes(int saltLength) {
        byte[] salt = new byte[saltLength];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);
        return salt;
    }

    /**
     * Concatena password e salt, poi calcola l'hash SHA-256.
     * 
     * @param password Password in byte array
     * @param salt Salt in byte array
     * @return Hash SHA-256 della concatenazione di password e salt
     * @throws RuntimeException se l'algoritmo di hashing non è disponibile
     */
    public static byte[] concatenateAndHash(byte[] password, byte[] salt) {
        try {
            // Alloca un nuovo array di byte con dimensioni totali
            byte[] concatenatedData = new byte[password.length + salt.length];

            // Copia i dati dal primo array di byte al nuovo array
            System.arraycopy(password, 0, concatenatedData, 0, password.length);

            // Copia i dati dal secondo array di byte al nuovo array
            System.arraycopy(salt, 0, concatenatedData, password.length, salt.length);

            // Ottieni un'istanza di MessageDigest con l'algoritmo SHA-256
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);

            // Calcola l'hash dell'array di byte concatenato
            return digest.digest(concatenatedData);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algoritmo di hashing non disponibile", e);
        }
    }

    /**
     * Verifica se una password rispetta i criteri di sicurezza.
     * Una password forte deve:
     * - Avere almeno 8 caratteri
     * - Contenere almeno una lettera maiuscola
     * - Contenere almeno un numero
     * - Contenere almeno un carattere speciale
     * 
     * @param password Password da verificare come array di byte
     * @return true se la password rispetta tutti i criteri, false altrimenti
     */
    public static boolean isStrongPassword(byte[] password) {
        if (password == null) {
            return false;
        }
        
        String passwordString = new String(password);

        return isLengthValid(passwordString) && 
               containsUpperCase(passwordString) && 
               containsDigit(passwordString) &&
               containsSpecialCharacter(passwordString);
    }

    /**
     * Verifica se la password ha la lunghezza minima richiesta.
     * 
     * @param password Password come stringa
     * @return true se la lunghezza è valida, false altrimenti
     */
    private static boolean isLengthValid(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }

    /**
     * Verifica se la password contiene almeno una lettera maiuscola.
     * 
     * @param password Password come stringa
     * @return true se contiene almeno una maiuscola, false altrimenti
     */
    private static boolean containsUpperCase(String password) {
        return password != null && Pattern.compile("[A-Z]").matcher(password).find();
    }

    /**
     * Verifica se la password contiene almeno un numero.
     * 
     * @param password Password come stringa
     * @return true se contiene almeno un numero, false altrimenti
     */
    private static boolean containsDigit(String password) {
        return password != null && Pattern.compile("\\d").matcher(password).find();
    }

    /**
     * Verifica se la password contiene almeno un carattere speciale.
     * 
     * @param password Password come stringa
     * @return true se contiene almeno un carattere speciale, false altrimenti
     */
    private static boolean containsSpecialCharacter(String password) {
        return password != null && 
               Pattern.compile("[" + Pattern.quote(SPECIAL_CHARACTERS) + "]").matcher(password).find();
    }
    
    /**
     * Verifica se una password corrisponde a quella archiviata.
     * 
     * @param inputPassword Password inserita dall'utente
     * @param storedHash Hash archiviato nel database
     * @param salt Salt archiviato nel database
     * @return true se la password corrisponde, false altrimenti
     */
    public static boolean verifyPassword(byte[] inputPassword, byte[] storedHash, byte[] salt) {
        byte[] hashedInput = concatenateAndHash(inputPassword, salt);
        
        // Controllo se gli hash hanno la stessa lunghezza
        if (hashedInput.length != storedHash.length) {
            return false;
        }
        
        // Confronto time-constant per evitare timing attacks
        int result = 0;
        for (int i = 0; i < hashedInput.length; i++) {
            result |= hashedInput[i] ^ storedHash[i];
        }
        
        return result == 0;
    }
}