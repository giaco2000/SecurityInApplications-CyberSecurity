package registration;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import javax.servlet.http.Part;

import database.DatabaseConnection;
import utils.MessageUtils;
import query.DatabaseQueries;
import security.PasswordManager;

/**
 * Classe per la gestione delle operazioni di database relative alla registrazione degli utenti.
 * Implementa metodi per salvare nuovi utenti e verificare se un utente esiste già.
 * 
 * @author Giacomo Pagliara
 */
public class RegistrationDao {

    /**
     * Registra un nuovo utente nel database, salvando username, password hashata, salt e immagine profilo.
     * Verifica prima se l'utente esiste già.
     * 
     * @param username Nome utente
     * @param hashedPassword Password hashata
     * @param salt Salt utilizzato per hashare la password
     * @param profileImagePart Immagine di profilo caricata
     * @return true se la registrazione è avvenuta con successo, false altrimenti
     * @throws IOException Se si verifica un errore durante la lettura dell'immagine
     */
    public static boolean registerUser(String username, byte[] hashedPassword, byte[] salt, Part profileImagePart)
            throws IOException {
        
        Connection writeConnection = null;
        Connection readConnection = null;
        boolean autoCommitOriginal = false;
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Ottieni connessioni separate per lettura e scrittura
            writeConnection = DatabaseConnection.getConnectionWrite();
            readConnection = DatabaseConnection.getConnectionRead();
            
            // Salva lo stato originale del autoCommit
            autoCommitOriginal = writeConnection.getAutoCommit();
            // Disabilita autoCommit per permettere transazioni
            writeConnection.setAutoCommit(false);

            // Verifica se l'utente esiste già
            if (userExistsCount(username, readConnection) > 0) {
                MessageUtils.showErrorMessage("Utente già registrato!");
                return false;
            }

            // Inserisci i dati utente principale
            boolean userInserted = insertUserData(username, hashedPassword, profileImagePart, writeConnection);
            
            // Se l'inserimento utente è riuscito, procedi con il salt
            if (userInserted) {
                boolean saltInserted = insertUserSalt(username, salt, writeConnection);
                
                if (saltInserted) {
                    // Commit della transazione se entrambe le operazioni sono riuscite
                    writeConnection.commit();
                    MessageUtils.showInfoMessage("Registrazione effettuata con successo!");
                    return true;
                } else {
                    // Rollback in caso di problemi con l'inserimento del salt
                    writeConnection.rollback();
                }
            } else {
                // Rollback se l'inserimento dell'utente non è riuscito
                writeConnection.rollback();
            }
            
        } catch (ClassNotFoundException | SQLException e) {
            if (writeConnection != null) {
                try {
                    // Rollback in caso di eccezione
                    writeConnection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
        	// Cancella in modo sicuro i dati sensibili dalla memoria
            if (hashedPassword != null) {
                PasswordManager.clearBytes(hashedPassword);
            }
            if (salt != null) {
                PasswordManager.clearBytes(salt);
            }
            // Ripristina lo stato originale di autoCommit
            if (writeConnection != null) {
                try {
                    writeConnection.setAutoCommit(autoCommitOriginal);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            
            // Chiusura delle connessioni
            closeConnection(writeConnection);
            closeConnection(readConnection);
        }
        
        MessageUtils.showErrorMessage("Non è stato possibile completare la registrazione");
        return false;
    }

    /**
     * Inserisce i dati utente principali (username, password, immagine profilo) nel database.
     * 
     * @param username Nome utente
     * @param hashedPassword Password hashata
     * @param profileImagePart Immagine di profilo
     * @param connection Connessione al database
     * @return true se l'inserimento è avvenuto con successo, false altrimenti
     * @throws SQLException Se si verifica un errore durante l'operazione SQL
     * @throws IOException Se si verifica un errore durante la lettura dell'immagine
     */
    private static boolean insertUserData(String username, byte[] hashedPassword, Part profileImagePart, Connection connection) 
            throws SQLException, IOException {
        
        try (PreparedStatement ps = connection.prepareStatement(DatabaseQueries.getRegistrationUserQuery())) {
            ps.setString(1, username);
            ps.setBytes(2, hashedPassword);

            InputStream fileContent = profileImagePart.getInputStream();
            ps.setBlob(3, fileContent);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Inserisce il salt per un utente nel database.
     * 
     * @param username Nome utente
     * @param salt Salt da memorizzare
     * @param connection Connessione al database
     * @return true se l'inserimento è avvenuto con successo, false altrimenti
     * @throws SQLException Se si verifica un errore durante l'operazione SQL
     */
    private static boolean insertUserSalt(String username, byte[] salt, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DatabaseQueries.getUserSaltQuery())) {
            ps.setString(1, username);
            ps.setBytes(2, salt);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Verifica se un utente esiste già nel database.
     * 
     * @param username Nome utente da verificare
     * @param connection Connessione al database
     * @return Il numero di utenti trovati con questo username
     * @throws SQLException Se si verifica un errore durante l'operazione SQL
     */
    private static int userExistsCount(String username, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DatabaseQueries.getUserExistsQuery())) {
            ps.setString(1, username);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        return 0;
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