package auth;

import java.sql.*;
import java.util.Arrays;

import database.DatabaseConnection;
import security.PasswordManager;
import query.DatabaseQueries;
import utils.MessageUtils;

/**
 * Classe per la gestione dell'autenticazione degli utenti nel database.
 * Verifica le credenziali utente confrontando username, password e salt.
 * 
 * @author Giacomo Pagliara
 */
public class AuthDao {
    
    /**
     * Verifica se un utente è valido nel database.
     * 
     * @param username Nome utente
     * @param password Password in chiaro (array di byte)
     * @return true se l'utente è valido, false altrimenti
     */
    public static boolean isUserValid(String username, byte[] password) {
        // LOG DI DEBUG
        System.out.println("Debug - AuthDao verifying: username='" + username + "', password length=" + password.length);
    	
        if (username == null || password == null) {
            return false;
        }
        
        Connection connection = null;
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DatabaseConnection.getConnectionRead();
            
            // Ottieni il salt dell'utente
            byte[] salt = getUserSalt(username, connection);
            if (salt == null) {
                MessageUtils.showErrorMessage("Utente non trovato");
                return false;
            }
            
            // Calcola l'hash della password con il salt
            byte[] hashedPassword = PasswordManager.concatenateAndHash(password, salt);
            
            // Verifica le credenziali
            boolean result = checkCredentials(username, hashedPassword, connection);
            
            // Pulisci i dati sensibili
            Arrays.fill(password, (byte) 0);
            Arrays.fill(salt, (byte) 0);
            Arrays.fill(hashedPassword, (byte) 0);
            
            return result;
            
        } catch (ClassNotFoundException | SQLException e) {
            MessageUtils.showErrorMessage("Errore durante la verifica dell'utente");
            e.printStackTrace();
            return false;
        } finally {
            closeConnection(connection);
        }
    }
    
    /**
     * Ottiene il salt di un utente dal database.
     * 
     * @param username Nome utente
     * @param connection Connessione al database
     * @return Array di byte contenente il salt, o null se non trovato
     * @throws SQLException Se si verifica un errore SQL
     */
    private static byte[] getUserSalt(String username, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(DatabaseQueries.getUserSaltRetrievalQuery())) {
            stmt.setString(1, username);
            
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    Blob saltBlob = resultSet.getBlob("salt");
                    if (saltBlob != null) {
                        return saltBlob.getBytes(1, (int) saltBlob.length());
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Verifica le credenziali dell'utente nel database.
     * 
     * @param username Nome utente
     * @param hashedPassword Password hashata
     * @param connection Connessione al database
     * @return true se le credenziali sono valide, false altrimenti
     * @throws SQLException Se si verifica un errore SQL
     */
    private static boolean checkCredentials(String username, byte[] hashedPassword, Connection connection) 
            throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(DatabaseQueries.getLoginQuery())) {
            stmt.setString(1, username);
            stmt.setBytes(2, hashedPassword);
            
            try (ResultSet resultSet = stmt.executeQuery()) {
                return resultSet.next();
            }
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