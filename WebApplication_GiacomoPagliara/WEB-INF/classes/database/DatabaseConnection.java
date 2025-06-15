package database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import utils.MessageUtils;
import utils.ConfigManager;

/**
 * Classe per la gestione delle connessioni al database.
 * Carica le configurazioni da un file di properties e fornisce
 * metodi per ottenere connessioni con privilegi diversi.
 * 
 * @author Giacomo Pagliara
 */
public class DatabaseConnection {
    private static String DATABASE_URL;
    private static String READ_USERNAME;
    private static String READ_PASSWORD;
    private static String WRITE_USERNAME;
    private static String WRITE_PASSWORD;
    
    static {
        try {
            Properties config = ConfigManager.getProperties();
            
            DATABASE_URL = config.getProperty("db.url");
            READ_USERNAME = config.getProperty("db.username_read");
            READ_PASSWORD = config.getProperty("db.password_read");
            WRITE_USERNAME = config.getProperty("db.username_write");
            WRITE_PASSWORD = config.getProperty("db.password_write");
            
        } catch (IOException e) {
            e.printStackTrace();
            MessageUtils.showErrorMessage("Errore nel caricamento del file di configurazione del database");
            throw new RuntimeException("Errore nel caricamento delle configurazioni del database", e);
        }
    }

    /**
     * Ottiene una connessione al database con privilegi di sola lettura.
     * 
     * @return Connessione con privilegi di lettura
     * @throws SQLException Se si verifica un errore nella connessione
     */
    public static Connection getConnectionRead() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL, READ_USERNAME, READ_PASSWORD);
    }

    /**
     * Ottiene una connessione al database con privilegi di scrittura.
     * 
     * @return Connessione con privilegi di scrittura
     * @throws SQLException Se si verifica un errore nella connessione
     */
    public static Connection getConnectionWrite() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL, WRITE_USERNAME, WRITE_PASSWORD);
    }
}