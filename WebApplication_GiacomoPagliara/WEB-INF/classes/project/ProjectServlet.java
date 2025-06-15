package project;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.google.gson.Gson;

import utils.MessageUtils;

/**
 * Servlet per la gestione delle proposte progettuali.
 * Gestisce il caricamento e la visualizzazione delle proposte.
 * 
 * @author Giacomo Pagliara
 */
@WebServlet("/ProjectServlet")
@MultipartConfig
public class ProjectServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Costanti per i parametri delle richieste
    private static final String PROJECT_FILE_PARAM = "Proposta progettuale";
    private static final String USERNAME_PARAM = "nomeUtente";

    /**
     * Costruttore predefinito.
     */
    public ProjectServlet() {
        super();
    }

    /**
     * Gestisce le richieste GET.
     * Restituisce tutte le proposte progettuali in formato JSON.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            // Recupera tutte le proposte progettuali
            List<Project> projects = Project.getAllProjects();
    
            System.out.println("Recuperate " + projects.size() + " proposte"); // Debug
            for (Project p : projects) {
                System.out.println("Proposta: " + p.getUsername() + ", " + p.getFileName());
            }
            
            // Converti la lista in JSON
            String jsonProjects = new Gson().toJson(projects);
            
            System.out.println("JSON generato: " + jsonProjects); // Debug
    
            // Imposta la risposta
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonProjects);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Si è verificato un errore nel recupero delle proposte.\"}");
        }
    }

    /**
     * Gestisce le richieste POST.
     * Carica una nuova proposta progettuale.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    	System.out.println("Nome utente ricevuto: " + request.getParameter("nomeUtente"));
    	System.out.println("File ricevuto: " + (request.getPart("Proposta progettuale") != null));
    	
        Part filePart = request.getPart(PROJECT_FILE_PARAM);
        String username = request.getParameter(USERNAME_PARAM);
        
        // Controllo input
        if (username == null || username.trim().isEmpty()) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Nome utente non specificato");
            return;
        }

        // Validazione file
        if (!ProjectFileValidator.isValidProjectFile(filePart, getServletContext())) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "File non valido");
            return;
        }

        // Processa il contenuto del file
        String sanitizedHtml = ProjectFileValidator.processFileContent(filePart);
        if (sanitizedHtml == null) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Impossibile processare il file");
            return;
        }

        String fileName = ProjectFileValidator.getFileName(filePart);
        byte[] htmlBytes = sanitizedHtml.getBytes(StandardCharsets.UTF_8);

        try {
            // Carica il file nel database
            if (ProjectDao.uploadProject(username, htmlBytes, fileName)) {
                MessageUtils.showInfoMessage("La proposta è stata correttamente caricata!");
                
             // Reindirizza alla pagina di visualizzazione delle proposte
                response.sendRedirect("progetti.jsp?action=view&success=true");
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "Non è stato possibile caricare il file della proposta");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Si è verificato un errore durante il caricamento");
        }
    }
    
    /**
     * Invia una risposta di errore al client.
     * 
     * @param response Risposta HTTP
     * @param statusCode Codice di stato HTTP
     * @param message Messaggio di errore
     * @throws IOException Se si verifica un errore di I/O
     */
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) 
            throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}