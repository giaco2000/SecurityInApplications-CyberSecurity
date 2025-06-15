package security;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Filtro di autenticazione
 */
@WebFilter(urlPatterns = {"/benvenuto.jsp", "/progetti.jsp"})
public class AuthenticationFilter implements Filter {
    
    private static final Logger logger = Logger.getLogger(AuthenticationFilter.class.getName());
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("AuthenticationFilter inizializzato");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        logger.info("AuthenticationFilter invocato per: " + httpRequest.getRequestURI());
        
        // Verifica se l'utente è già autenticato
        HttpSession session = httpRequest.getSession(false);
        boolean isLoggedIn = (session != null && session.getAttribute("login") != null && 
                (Boolean) session.getAttribute("login"));
        
        logger.info("Stato sessione: " + (session != null ? "Esistente" : "Non esistente"));
        logger.info("Stato login: " + (isLoggedIn ? "Loggato" : "Non loggato"));
        
        if (isLoggedIn) {
            // Utente già autenticato, prosegui
            logger.info("Utente autenticato via sessione, accesso consentito");
            chain.doFilter(request, response);
            return;
        }
        
        // Verifica se c'è un cookie rememberToken
        String cookieValue = null;
        Cookie[] cookies = httpRequest.getCookies();
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("rememberToken".equals(cookie.getName())) {
                    cookieValue = cookie.getValue();
                    logger.info("Cookie rememberToken trovato: " + cookieValue);
                    break;
                }
            }
        }
        
        if (cookieValue != null) {
            //  Estrai UUID e token dal valore del cookie
            String[] parts = cookieValue.split(":", 2);
            if (parts.length != 2) {
                logger.warning("Formato cookie non valido: " + cookieValue);
                removeCookie(httpResponse);
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp?unauthorized=true");
                return;
            }
            
            String uuid = parts[0];
            String encryptedToken = parts[1];
            
            logger.info("UUID estratto: " + uuid);
            logger.info("Token cifrato estratto: [OMESSO PER SICUREZZA]");
            
            //  Verifica il token 
            String username = TokenManager.validateToken(encryptedToken, uuid);
            logger.info("Validazione token per username: " + username);
            
            if (username != null) {
                // Token valido, crea una nuova sessione
                HttpSession newSession = httpRequest.getSession(true);
                newSession.setAttribute("login", true);
                newSession.setAttribute("nomeUtente", username);
                newSession.setMaxInactiveInterval(15*60); // 15 minuti
                
                logger.info("Autenticazione via token riuscita, nuova sessione creata per: " + username);
                
                // Continua con la richiesta
                chain.doFilter(request, response);
                return;
            } else {
                logger.info("Token non valido, rimuovo il cookie");
                // Token non valido, rimuovi il cookie
                removeCookie(httpResponse);
            }
        } else {
            logger.info("Nessun cookie rememberToken trovato");
        }
        
        // Nessuna autenticazione valida, reindirizza al login
        logger.warning("ACCESSO NEGATO: reindirizzamento a login.jsp");
        httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp?unauthorized=true");
    }
    
    /**
     * Rimuove il cookie rememberToken
     * 
     * @param response Risposta HTTP
     */
    private void removeCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("rememberToken", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
    
    @Override
    public void destroy() {
        logger.info("AuthenticationFilter distrutto");
    }
}