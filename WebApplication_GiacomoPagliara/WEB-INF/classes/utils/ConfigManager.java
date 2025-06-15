package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Gestore centralizzato per la configurazione dell'applicazione.
 * Carica e fornisce accesso alle proprietà configurate nel file config.ini.
 * 
 * @author Giacomo Pagliara
 */
public class ConfigManager {
    // Percorso del file di configurazione
    private static final String CONFIG_FILE = "config.ini";
    // Proprietà caricate dal file
    private static Properties properties = null;
    
    /**
     * Ottiene le proprietà di configurazione, caricandole se necessario.
     * 
     * @return Oggetto Properties con le configurazioni
     * @throws IOException Se si verifica un errore durante il caricamento
     */
    public static synchronized Properties getProperties() throws IOException {
        if (properties == null) {
            properties = loadProperties();
        }
        return properties;
    }
    
    /**
     * Carica le proprietà dal file di configurazione.
     * 
     * @return Oggetto Properties con le configurazioni caricate
     * @throws IOException Se si verifica un errore durante il caricamento
     */
    private static Properties loadProperties() throws IOException {
        Properties props = new Properties();
        
        try (InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            
            if (input == null) {
                throw new IOException("File di configurazione " + CONFIG_FILE + " non trovato");
            }
            
            props.load(input);
            return props;
        }
    }
    
    /**
     * Ottiene il valore di una proprietà specifica.
     * 
     * @param key Chiave della proprietà da recuperare
     * @return Valore della proprietà o null se non esiste
     * @throws IOException Se si verifica un errore durante il caricamento
     */
    public static String getProperty(String key) throws IOException {
        return getProperties().getProperty(key);
    }
}