package auth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.JsonObject;

import security.AesEncryption;
import security.PasswordManager;
import security.TokenManager;
import utils.MessageUtils;

/**
 * Servlet per gestire il login degli utenti.
 * Supporta l'autenticazione con/senza meccanismo "ricordami" tramite cookie.
 * 
 * @author Giacomo Pagliara
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Costanti per i parametri e gli attributi
    private static final String USERNAME_PARAM = "username";
    private static final String PASSWORD_PARAM = "password";
    private static final String REMEMBER_ME_PARAM = "ricordami";
    private static final String LOGIN_ATTR = "login";
    private static final String USERNAME_ATTR = "nomeUtente";
    
    // Costanti per i cookie
    private static final String REMEMBER_TOKEN_COOKIE = "rememberToken";
    // Durata di un giorno in secondi
    private static final int COOKIE_MAX_AGE = 24 * 60 * 60; 
    
    // Costanti per le pagine
    private static final String WELCOME_PAGE = "benvenuto.jsp";
    private static final String LOGIN_PAGE = "login.jsp";

    /**
     * Costruttore predefinito.
     */
    public LoginServlet() {
        super();
    }

    /**
     * Gestisce le richieste GET.
     * Verifica la presenza di cookie di autenticazione.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        checkCookie(request, response);
    }
    
    /**
     * Gestisce le richieste POST.
     * Processa il form di login e gestisce l'autenticazione.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Configurazione della codifica
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        
        // Ottiene i parametri dal form
        String username = request.getParameter(USERNAME_PARAM);
        String passwordStr = request.getParameter(PASSWORD_PARAM);
        byte[] password;

        // Inizializza la password
        if (passwordStr != null && passwordStr.length() > 0) {
            try {
                // Prova a decodificare come Base64
                password = Base64.getDecoder().decode(passwordStr);
                System.out.println("Password decodificata da Base64");
            } catch (IllegalArgumentException e) {
                // Non è Base64, usa come testo normale
                password = passwordStr.getBytes(StandardCharsets.UTF_8);
                System.out.println("Password usata come testo normale");
            }
        } else {
            password = new byte[0];
        }
        
        boolean rememberMe = request.getParameter(REMEMBER_ME_PARAM) != null;
        
        // LOG DI DEBUG
        System.out.println("Debug - Login attempt: username='" + username + "', password length=" + password.length);
        
        try {
            // Verifica le credenziali dell'utente
            if (AuthDao.isUserValid(username, password)) {
                // Imposta gli attributi di sessione
                request.setAttribute(LOGIN_ATTR, true);
                request.setAttribute(USERNAME_ATTR, username);
                
                // Imposta anche gli attributi nella sessione HTTP
                HttpSession session = request.getSession();
                session.setAttribute(LOGIN_ATTR, true);
                session.setAttribute(USERNAME_ATTR, username);
                
                // Se "ricordami" è selezionato, crea i cookie
                if (rememberMe) {
                	createAuthCookies(username, response);
                }
                
                // Pulisci i dati sensibili
                PasswordManager.clearBytes(password);
                username = null;
                
                // Reindirizza alla pagina di benvenuto
                response.sendRedirect(WELCOME_PAGE);
            } else {
                // Autenticazione fallita
                PasswordManager.clearBytes(password);
                username = null;
                
                MessageUtils.showErrorMessage("Credenziali non valide. Riprova.");
                response.sendRedirect(LOGIN_PAGE);
            }
        } catch (Exception e) {
            // Gestione degli errori
            PasswordManager.clearBytes(password);
            username = null;
            
            MessageUtils.showErrorMessage("Si è verificato un errore durante il login.");
            e.printStackTrace();
            response.sendRedirect("login.jsp");
        }
    }
    
    /**
     * Crea e aggiunge i cookie di autenticazione alla risposta.
     * 
     * @param username Nome utente
     * @param response Risposta HTTP
     */
    private void createAuthCookies(String username, HttpServletResponse response) {
        try {
            // Genera un token di autenticazione
            TokenManager.TokenResult tokenResult = TokenManager.generateRememberToken(username);
            if (tokenResult != null) {
                // Creiamo un cookie che contiene sia il token cifrato che l'UUID
                String encryptedToken = AesEncryption.encryptToBase64(tokenResult.getPlainToken());
                String cookieValue = tokenResult.getUuid() + ":" + encryptedToken;
                
                // Crea e configura il cookie
                Cookie rememberMeCookie = new Cookie(REMEMBER_TOKEN_COOKIE, cookieValue);
                rememberMeCookie.setHttpOnly(true);
                rememberMeCookie.setSecure(true);
                rememberMeCookie.setMaxAge(COOKIE_MAX_AGE); // 1 giorno in secondi
                
                // Aggiungi il cookie alla risposta
                response.addCookie(rememberMeCookie);
                
                System.out.println("Cookie di autenticazione creato con successo: " + tokenResult.getUuid());
            }
        } catch (Exception e) {
            System.out.println("Errore nella creazione del cookie di autenticazione: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Verifica la presenza e la validità dei cookie di autenticazione.
     * 
     * @param request Richiesta HTTP
     * @param response Risposta HTTP
     * @throws IOException Se si verifica un errore di I/O
     * @throws ServletException Se si verifica un errore nella servlet
     */
    private void checkCookie(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, ServletException {
        
        // Configurazione della codifica
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        
        JsonObject cookieData = new JsonObject();
        cookieData.addProperty("cookiesPresent", false);
        
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            response.getWriter().write(cookieData.toString());
            return;
        }
        
        String cookieValue = null;
        
        // Estrai il cookie del token
        for (Cookie cookie : cookies) {
            if (REMEMBER_TOKEN_COOKIE.equals(cookie.getName())) {
                cookieValue = cookie.getValue();
            } else if (!"JSESSIONID".equals(cookie.getName())) {
                // Invalida i cookie non riconosciuti
                invalidateCookie(cookie, response);
            }
        }
        
        // Se non c'è token, termina
        if (cookieValue == null) {
            response.getWriter().write(cookieData.toString());
            return;
        }
        
        // Estrai UUID e token dal valore del cookie
        String[] parts = cookieValue.split(":", 2);
        if (parts.length != 2) {
            // Formato cookie non valido
            response.getWriter().write(cookieData.toString());
            return;
        }
        
        String uuid = parts[0];
        String encryptedToken = parts[1];
        
        // Verifica il token usando UUID e token criptato
        String username = TokenManager.validateToken(encryptedToken, uuid);
        if (username != null) {
            // Token valido
            cookieData.addProperty("cookiesPresent", true);
            cookieData.addProperty("username", username);
            
            // Imposta anche se l'utente è già autenticato
            HttpSession session = request.getSession(false);
            boolean authenticated = (session != null && 
                    session.getAttribute(LOGIN_ATTR) != null && 
                    (Boolean)session.getAttribute(LOGIN_ATTR));
            cookieData.addProperty("authenticated", authenticated);
            
            // Se il token è valido ma non c'è sessione attiva, crea una nuova sessione
            if (!authenticated) {
                HttpSession newSession = request.getSession(true);
                newSession.setAttribute(LOGIN_ATTR, true);
                newSession.setAttribute(USERNAME_ATTR, username);
                newSession.setMaxInactiveInterval(15*60); // 15 minuti
                cookieData.addProperty("authenticated", true);
            }
        }
        
        response.getWriter().write(cookieData.toString());
    }
    
    /**
     * Invalida un cookie impostando il suo valore a vuoto e la scadenza a 0.
     * 
     * @param cookie Cookie da invalidare
     * @param response Risposta HTTP
     */
    private void invalidateCookie(Cookie cookie, HttpServletResponse response) {
        cookie.setValue("");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}