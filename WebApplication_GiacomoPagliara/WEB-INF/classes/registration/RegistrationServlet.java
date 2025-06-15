package registration;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import security.PasswordManager;
import utils.MessageUtils;

/**
 * Servlet per gestire la registrazione degli utenti.
 * Gestisce la richiesta di registrazione, validando i dati di input e
 * interagendo con il database tramite RegistrationDao.
 * 
 * @author Giacomo Pagliara
 */
@MultipartConfig
@WebServlet("/RegistrationServlet")
public class RegistrationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Costanti per la validazione
    private static final String USERNAME_REGEX = "^[a-zA-Z0-9]+$";
    private static final int MAX_USERNAME_LENGTH = 45;
    private static final int SALT_LENGTH = 16;

    /**
     * Costruttore predefinito.
     */
    public RegistrationServlet() {
        super();
    }

    /**
     * Gestisce le richieste POST per la registrazione degli utenti.
     * Valida i dati di input, crea un nuovo utente e reindirizza di conseguenza.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Ottenimento dei parametri dalla richiesta
        String username = request.getParameter("username");
        byte[] password = request.getParameter("password").getBytes();
        byte[] confirmPassword = request.getParameter("conferma_password").getBytes();
        Part profileImagePart = request.getPart("ImmagineProfilo");
        
        // Array per tenere traccia dei dati sensibili da cancellare
        byte[][] sensitiveData = new byte[3][];
        sensitiveData[0] = password;
        sensitiveData[1] = confirmPassword;
        
        try {
            // Validazione username
            if (!isValidUsername(username, request, response)) {
                clearSensitiveData(sensitiveData);
                return;
            }
            
            // Validazione della password
            if (!PasswordManager.isStrongPassword(password)) {
                MessageUtils.showErrorMessage("La password non rispetta i requisiti minimi di sicurezza!");
                request.getRequestDispatcher("registration.jsp").forward(request, response);
                clearSensitiveData(sensitiveData);
                return;
            }
            
            // Confronto delle password
            if (!Arrays.equals(password, confirmPassword)) {
                MessageUtils.showErrorMessage("Le password non corrispondono!");
                request.getRequestDispatcher("registration.jsp").forward(request, response);
                clearSensitiveData(sensitiveData);
                return;
            }
            
            // Validazione dell'immagine del profilo
            if (!FileValidator.isValidImageFile(profileImagePart)) {
                request.getRequestDispatcher("registration.jsp").forward(request, response);
                clearSensitiveData(sensitiveData);
                return;
            }
            
            // Preparazione e hashing della password
            byte[] salt = PasswordManager.generateRandomBytes(SALT_LENGTH);
            byte[] hashedPassword = PasswordManager.concatenateAndHash(password, salt);
            sensitiveData[2] = salt;
            
            // Registrazione dell'utente
            if (RegistrationDao.registerUser(username, hashedPassword, salt, profileImagePart)) {
                // Registrazione avvenuta con successo
                response.sendRedirect("login.jsp");
            } else {
                // Errore durante la registrazione
                request.getRequestDispatcher("registration.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            MessageUtils.showErrorMessage("Errore durante la registrazione!");
            request.getRequestDispatcher("registration.jsp").forward(request, response);
        } finally {
            // Pulizia dei dati sensibili
            clearSensitiveData(sensitiveData);
            username = null;
        }
    }
    
    /**
     * Valida il nome utente.
     * 
     * @param username Nome utente da validare
     * @param request Richiesta HTTP
     * @param response Risposta HTTP
     * @return true se il nome utente è valido, false altrimenti
     */
    private boolean isValidUsername(String username, HttpServletRequest request, 
            HttpServletResponse response) throws ServletException, IOException {
        
        if (username == null || !username.matches(USERNAME_REGEX) || username.length() > MAX_USERNAME_LENGTH) {
            MessageUtils.showErrorMessage("Il nome contiene caratteri non validi o è troppo lungo!");
            request.getRequestDispatcher("registration.jsp").forward(request, response);
            return false;
        }
        return true;
    }
    
    /**
     * Cancella in modo sicuro tutti i dati sensibili.
     * 
     * @param sensitiveData Array di array di byte contenenti dati sensibili
     */
    private void clearSensitiveData(byte[][] sensitiveData) {
        for (byte[] data : sensitiveData) {
            if (data != null) {
                PasswordManager.clearBytes(data);
            }
        }
    }
}