package security;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Calendar;
import java.util.UUID;

import database.DatabaseConnection;
import query.DatabaseQueries;
import utils.MessageUtils;

/**
 * Classe per la gestione dei token di autenticazione "ricordami".
 * Implementa funzionalità per generare, verificare e invalidare token.
 * 
 * @author Giacomo Pagliara
 */
public class TokenManager {
    
    // Durata dei token in giorni
    private static final int TOKEN_DURATION_DAYS = 1;
    
    // Lunghezza del token in byte
    private static final int TOKEN_BYTE_LENGTH = 32;
    
    /**
     * Classe per restituire sia il token in chiaro che l'UUID
     */
    public static class TokenResult {
        private String plainToken;
        private String uuid;
        
        public TokenResult(String plainToken, String uuid) {
            this.plainToken = plainToken;
            this.uuid = uuid;
        }
        
        public String getPlainToken() { return plainToken; }
        public String getUuid() { return uuid; }
    }
    
    /**
     * Genera un nuovo token univoco per l'utente e lo memorizza nel database.
     * 
     * @param username Nome utente
     * @return TokenResult contenente token generato e UUID o null in caso di errore
     */
    public static TokenResult generateRememberToken(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        
        // Genera UUID per identificare il token
        String uuid = UUID.randomUUID().toString();
        
        // Genera un token casuale sicuro
        byte[] randomBytes = new byte[TOKEN_BYTE_LENGTH];
        new SecureRandom().nextBytes(randomBytes);
        String plainToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        
        // Cripta il token con AES
        String encryptedToken;
        try {
            encryptedToken = AesEncryption.encryptToBase64(plainToken);
        } catch (Exception e) {
            MessageUtils.showErrorMessage("Errore durante la crittografia del token");
            e.printStackTrace();
            return null;
        }
        
        // Calcola la data di scadenza (1 giorno da ora)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, TOKEN_DURATION_DAYS);
        Timestamp expiryDate = new Timestamp(calendar.getTimeInMillis());
        
        // Salva il token criptato nel database
        Connection connection = null;
        try {
            // Prima elimina eventuali token esistenti per l'utente
            deleteTokensByUsername(username);
            
            connection = DatabaseConnection.getConnectionWrite();
            // Query modificata per includere l'UUID
            String query = DatabaseQueries.getInsertRememberTokenQuery();
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, encryptedToken);
                stmt.setString(3, uuid);
                stmt.setTimestamp(4, expiryDate);
                stmt.executeUpdate();
                return new TokenResult(plainToken, uuid);
            }
        } catch (SQLException e) {
            MessageUtils.showErrorMessage("Errore durante la generazione del token");
            e.printStackTrace();
            return null;
        } finally {
            closeConnection(connection);
        }
    }
    
    /**
     * Verifica se un token è valido e restituisce il nome utente associato.
     * 
     * @param encryptedToken Token cifrato da verificare
     * @param uuid UUID associato al token
     * @return Nome utente associato al token o null se non valido
     */
    public static String validateToken(String encryptedToken, String uuid) {
        if (encryptedToken == null || encryptedToken.isEmpty() || uuid == null || uuid.isEmpty()) {
            return null;
        }
        
        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnectionRead();
            
            // Query modificata per cercare per UUID
            String query = DatabaseQueries.getCheckRememberTokenQuery();
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, uuid);
                try (ResultSet resultSet = stmt.executeQuery()) {
                    if (resultSet.next()) {
                        String username = resultSet.getString("username");
                        String storedEncryptedToken = resultSet.getString("token");
                        
                        try {
                            // Decripta entrambi i token
                            String plainStoredToken = AesEncryption.decryptFromBase64(storedEncryptedToken);
                            String plainCookieToken = AesEncryption.decryptFromBase64(encryptedToken);
                            
                            // Confronta i token in chiaro
                            if (plainStoredToken.equals(plainCookieToken)) {
                                return username;
                            }
                        } catch (Exception e) {
                            System.out.println("Errore nella decriptazione dei token: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            MessageUtils.showErrorMessage("Errore durante la validazione del token");
            e.printStackTrace();
        } finally {
            closeConnection(connection);
        }
        
        return null;
    }
    
    /**
     * Elimina tutti i token associati a un utente.
     * 
     * @param username Nome utente
     * @return true se l'operazione ha successo, false altrimenti
     */
    public static boolean deleteTokensByUsername(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        
        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnectionWrite();
            String query = DatabaseQueries.getDeleteRememberTokenQuery();
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            MessageUtils.showErrorMessage("Errore durante l'eliminazione dei token");
            e.printStackTrace();
            return false;
        } finally {
            closeConnection(connection);
        }
    }
    
    /**
     * Elimina un token specifico tramite UUID.
     * 
     * @param uuid UUID del token da eliminare
     * @return true se l'operazione ha successo, false altrimenti
     */
    public static boolean deleteTokenByUuid(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return false;
        }
        
        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnectionWrite();
            try (PreparedStatement stmt = connection.prepareStatement(DatabaseQueries.getDeleteTokenByUuidQuery())) {
                stmt.setString(1, uuid);
                stmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            MessageUtils.showErrorMessage("Errore durante l'eliminazione del token");
            e.printStackTrace();
            return false;
        } finally {
            closeConnection(connection);
        }
    }
    
    /**
     * Elimina tutti i token scaduti dal database.
     * 
     * @return true se l'operazione ha successo, false altrimenti
     */
    public static boolean cleanExpiredTokens() {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnectionWrite();
            try (PreparedStatement stmt = connection.prepareStatement(DatabaseQueries.getDeleteExpiredTokensQuery())) {
                stmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeConnection(connection);
        }
    }
    
    /**
     * Chiude in modo sicuro una connessione al database.
     * 
     * @param connection Connessione da chiudere
     */
    private static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}