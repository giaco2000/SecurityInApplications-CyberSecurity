package project;

import java.io.IOException;
import java.sql.*;

import database.DatabaseConnection;
import utils.MessageUtils;
import query.DatabaseQueries;

/**
 * Classe per la gestione dell'accesso ai dati delle proposte progettuali.
 * Fornisce metodi per caricare le proposte nel database.
 * 
 * @author Giacomo Pagliara
 */
public class ProjectDao {

    /**
     * Carica un file di proposta progettuale nel database.
     * 
     * @param username Nome utente del proprietario
     * @param fileContent Contenuto del file
     * @param fileName Nome del file
     * @return true se il caricamento è avvenuto con successo, false altrimenti
     * @throws IOException Se si verifica un errore di I/O
     * @throws SQLException Se si verifica un errore SQL
     */
    public static boolean uploadProject(String username, byte[] fileContent, String fileName) 
            throws IOException, SQLException {
        
        Connection connection = null;
        boolean success = false;
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DatabaseConnection.getConnectionWrite();
            
            try (PreparedStatement ps = connection.prepareStatement(DatabaseQueries.getInsertProposalQuery())) {
                ps.setString(1, username);
                ps.setBytes(2, fileContent);
                ps.setString(3, fileName);

                int rowsAffected = ps.executeUpdate();
                success = rowsAffected > 0;
                
                if (!success) {
                    MessageUtils.showErrorMessage("Nessuna riga è stata modificata durante l'inserimento della proposta.");
                }
            }
        } catch (ClassNotFoundException e) {
            MessageUtils.showErrorMessage("Driver del database non trovato.");
            throw new SQLException("Driver del database non trovato", e);
        } catch (SQLException e) {
            MessageUtils.showErrorMessage("Si è verificato un errore durante l'inserimento della proposta.");
            throw e;
        } finally {
            closeConnection(connection);
        }
        
        return success;
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