package session;

import java.io.IOException;
import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import security.TokenManager;
import utils.MessageUtils;

/**
 * Servlet per gestire il logout degli utenti.
 * Invalida la sessione e rimuove tutti i cookie.
 * Questa servlet è thread-safe e può essere utilizzata in ambienti concorrenti.
 * 
 * @author Giacomo Pagliara
 */
@ThreadSafe
@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Pagina di reindirizzamento dopo il logout
    private static final String LOGIN_PAGE = "login.jsp";
    
    /**
     * Gestisce le richieste GET.
     * Esegue il logout dell'utente invalidando la sessione e i cookie.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Verifica se è una richiesta di timeout
        boolean isTimeout = request.getParameter("timeout") != null && 
                           request.getParameter("timeout").equals("true");
        
        // Ottieni informazioni sulla sessione e username
        HttpSession session = request.getSession(false);
        String username = null;
        
        if (session != null) {
            username = (String) session.getAttribute("nomeUtente");
            
            // Invalida la sessione
            session.invalidate();
            System.out.println("Sessione invalidata");
        }
        
        if (!isTimeout) {
            // Caso di logout normale (non timeout)
            
            // Rimuovi i cookie
            removeCookies(request, response);
            
            // Elimina i token associati all'utente
            if (username != null) {
                TokenManager.deleteTokensByUsername(username);
                System.out.println("Token eliminati per l'utente: " + username);
            }
            
            // Notifica all'utente
            MessageUtils.showInfoMessage("Logout effettuato con successo!");
            
            // Reindirizza alla pagina di login
            response.sendRedirect(LOGIN_PAGE);
        } else {
            // Caso di timeout di sessione
            
            // Verifica se l'utente aveva un cookie rememberToken
            boolean hasRememberToken = false;
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("rememberToken".equals(cookie.getName())) {
                        hasRememberToken = true;
                        break;
                    }
                }
            }
            
            // Se NON aveva il cookie di rememberToken, tratta come un logout completo
            if (!hasRememberToken && username != null) {
                // Rimuovi i cookie comunque per sicurezza
                removeCookies(request, response);
                
                // Elimina i token associati all'utente
                TokenManager.deleteTokensByUsername(username);
                System.out.println("Token eliminati per l'utente: " + username);
            }
            
            // Invia solo un codice di successo, il resto è gestito via JavaScript
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }
    
    /**
     * Rimuove tutti i cookie dalla richiesta.
     * 
     * @param request Richiesta HTTP
     * @param response Risposta HTTP
     */
    private void removeCookies(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                // Per il cookie rememberToken, eliminalo anche dal database
                if ("rememberToken".equals(cookie.getName())) {
                    String cookieValue = cookie.getValue();
                    if (cookieValue != null && !cookieValue.isEmpty()) {
                        String[] parts = cookieValue.split(":", 2);
                        if (parts.length == 2) {
                            String uuid = parts[0];
                            TokenManager.deleteTokenByUuid(uuid);
                            System.out.println("Token eliminato con UUID: " + uuid);
                        }
                    }
                    
                    // Crea un nuovo cookie con gli stessi attributi
                    Cookie killCookie = new Cookie("rememberToken", "");
                    killCookie.setMaxAge(0);
                    killCookie.setPath("/");  // Usa lo stesso path del cookie originale
                    
                    // Aggiungi gli stessi attributi usati in LoginServlet
                    killCookie.setHttpOnly(true);
                    killCookie.setSecure(true);
                    
                    // Se l'applicazione usa un context path
                    if (request.getContextPath() != null && !request.getContextPath().isEmpty()) {
                        killCookie.setPath(request.getContextPath());
                    }
                    
                    response.addCookie(killCookie);
                    System.out.println("Cookie rememberToken rimosso con parametri completi");
                } else {
                    // Per altri cookie, usa il metodo standard
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
                
                System.out.println("Nome del cookie da rimuovere: " + cookie.getName());
            }
            System.out.println("Cookie invalidati");
        }
    }
}