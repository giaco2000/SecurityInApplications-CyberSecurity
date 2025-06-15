package query;

import java.io.IOException;
import java.util.Properties;

import utils.ConfigManager;

/**
 * Classe per la gestione centralizzata delle query SQL.
 * Recupera le query dal file di configurazione, evitando duplicazioni
 * e garantendo coerenza.
 * 
 * @author Giacomo Pagliara
 */
public class DatabaseQueries {
    // Costanti per i nomi delle proprietà
    private static final String USER_LOGIN_QUERY = "db.query_userLogin";
    private static final String USER_REGISTRATION_QUERY = "db.query_userRegistration";
    private static final String USER_SALT_QUERY = "db.query_userSale";
    private static final String USER_ALREADY_EXISTS_QUERY = "db.query_userAlreadyExist";
    private static final String TAKE_USER_SALT_QUERY = "db.query_takeUserSale";
    private static final String INSERT_PROPOSAL_QUERY = "db.query_insertProposta";
    private static final String GET_USERNAMES_AND_PROPOSALS_QUERY = "db.query_takeUsernameAndProposta";
    // Aggiungi queste costanti
    private static final String INSERT_REMEMBER_TOKEN_QUERY = "db.query_insertRememberToken";
    private static final String CHECK_REMEMBER_TOKEN_QUERY = "db.query_checkRememberToken";
    private static final String DELETE_REMEMBER_TOKEN_QUERY = "db.query_deleteRememberToken";
    private static final String DELETE_EXPIRED_TOKENS_QUERY = "db.query_deleteExpiredTokens";
    // Costante per eliminazione per UUID
    private static final String DELETE_TOKEN_BY_UUID_QUERY = "db.query_deleteTokenByUuid";
    
    /**
     * Ottiene la query per il login dell'utente.
     * 
     * @return Query SQL per il login
     */
    public static String getLoginQuery() {
        return getQueryProperty(USER_LOGIN_QUERY);
    }
    
    /**
     * Ottiene la query per la registrazione dell'utente.
     * 
     * @return Query SQL per la registrazione
     */
    public static String getRegistrationUserQuery() {
        return getQueryProperty(USER_REGISTRATION_QUERY);
    }
    
    /**
     * Ottiene la query per salvare il salt dell'utente.
     * 
     * @return Query SQL per il salvataggio del salt
     */
    public static String getUserSaltQuery() {
        return getQueryProperty(USER_SALT_QUERY);
    }
    
    /**
     * Ottiene la query per verificare se un utente esiste già.
     * 
     * @return Query SQL per verificare l'esistenza
     */
    public static String getUserExistsQuery() {
        return getQueryProperty(USER_ALREADY_EXISTS_QUERY);
    }
    
    /**
     * Ottiene la query per recuperare il salt di un utente.
     * 
     * @return Query SQL per recuperare il salt
     */
    public static String getUserSaltRetrievalQuery() {
        return getQueryProperty(TAKE_USER_SALT_QUERY);
    }
    
    /**
     * Ottiene la query per inserire una proposta progettuale.
     * 
     * @return Query SQL per inserire una proposta
     */
    public static String getInsertProposalQuery() {
        return getQueryProperty(INSERT_PROPOSAL_QUERY);
    }
    
    /**
     * Ottiene la query per recuperare utenti e proposte.
     * 
     * @return Query SQL per recuperare utenti e proposte
     */
    public static String getUsersAndProposalsQuery() {
        return getQueryProperty(GET_USERNAMES_AND_PROPOSALS_QUERY);
    }
    
    //  metodi per il token della funzionalita "ricordami"
    public static String getInsertRememberTokenQuery() {
        return getQueryProperty(INSERT_REMEMBER_TOKEN_QUERY);
    }

    public static String getCheckRememberTokenQuery() {
        return getQueryProperty(CHECK_REMEMBER_TOKEN_QUERY);
    }

    public static String getDeleteRememberTokenQuery() {
        return getQueryProperty(DELETE_REMEMBER_TOKEN_QUERY);
    }

    public static String getDeleteExpiredTokensQuery() {
        return getQueryProperty(DELETE_EXPIRED_TOKENS_QUERY);
    }
    
    /**
     * Ottiene la query per eliminare un token "ricordami" specifico per UUID.
     * 
     * @return Query SQL per l'eliminazione del token per UUID
     */
    public static String getDeleteTokenByUuidQuery() {
        return getQueryProperty(DELETE_TOKEN_BY_UUID_QUERY);
    }
    
    /**
     * Recupera una query specifica dal file di configurazione.
     * 
     * @param propertyName Nome della proprietà da recuperare
     * @return Valore della proprietà o null in caso di errore
     */
    private static String getQueryProperty(String propertyName) {
        try {
            Properties properties = ConfigManager.getProperties();
            return properties.getProperty(propertyName);
        } catch (IOException e) {
            System.err.println("Errore nel recupero della query " + propertyName);
            e.printStackTrace();
            return null;
        }
    }
}