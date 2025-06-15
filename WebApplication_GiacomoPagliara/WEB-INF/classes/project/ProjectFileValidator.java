package project;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletContext;
import javax.servlet.http.Part;

import org.apache.tika.Tika;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

import utils.MessageUtils;

/**
 * Classe per la validazione e l'elaborazione sicura dei file delle proposte progettuali.
 * 
 * @author Giacomo Pagliara
 */
public class ProjectFileValidator {
    
    // Costanti per la validazione
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20 MB
    private static final String ALLOWED_EXTENSION = "txt";
    private static final String TEXT_MIME_TYPE = "text/plain";
    private static final String HTML_MIME_TYPE = "text/html";

    /**
     * Verifica se un file è una proposta progettuale valida.
     * 
     * @param filePart Parte del file caricato
     * @param context Contesto della servlet
     * @return true se il file è valido, false altrimenti
     * @throws IOException Se si verifica un errore durante la lettura del file
     */
    public static boolean isValidProjectFile(Part filePart, ServletContext context) throws IOException {
        // Controlla se il file è stato effettivamente caricato
        if (filePart == null || filePart.getSize() <= 0) {
            MessageUtils.showErrorMessage("Devi caricare una proposta progettuale.");
            return false;
        }
        
        // Ottieni il nome del file
        String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
        
        // Controlla l'estensione del file
        String fileExtension = getFileExtension(fileName);
        if (!ALLOWED_EXTENSION.equals(fileExtension)) {
            MessageUtils.showErrorMessage("Puoi caricare solo file di testo in formato txt.");
            return false;
        }
        
        // Verifica il percorso del file (solo per logging)
        String realPath = context.getRealPath("/");
        Path filePath = Paths.get(realPath, fileName);
        System.out.println("File path: " + filePath);
        
        return true;
    }
    
    /**
     * Processa il contenuto di un file, sanitizzando l'HTML per prevenire XSS.
     * 
     * @param filePart Parte del file caricato
     * @return Contenuto sanitizzato del file o null se non valido
     */
    public static String processFileContent(Part filePart) {
        try {
            // Verifica dimensione file
            if (filePart.getSize() > MAX_FILE_SIZE) {
                MessageUtils.showErrorMessage(
                        "Il file supera la dimensione massima consentita. Il file può essere massimo di 20 MB");
                return null;
            }

            // Verifica tipo MIME
            Tika tika = new Tika();
            String contentType = tika.detect(filePart.getInputStream());
            
            if (!TEXT_MIME_TYPE.equals(contentType) && !HTML_MIME_TYPE.equals(contentType)) {
                MessageUtils.showErrorMessage("Il file contiene del testo non valido.");
                return null;
            }
            
            // Leggi il contenuto del file
            String content = readFileContent(filePart);
            
            // Sanitizza il contenuto HTML
            return sanitizeHtml(content);
            
        } catch (IOException e) {
            e.printStackTrace();
            MessageUtils.showErrorMessage("C'è stato un problema con il caricamento del file.");
            return null;
        }
    }
    
    /**
     * Ottiene il nome del file da una parte multipart.
     * 
     * @param part Parte multipart
     * @return Nome del file
     */
    public static String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        String[] tokens = contentDisposition.split(";");
        
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                String fileNameWithPath = token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
                return getFileNameFromPath(fileNameWithPath);
            }
        }
        
        return "";
    }
    
    /**
     * Legge il contenuto di un file da una parte multipart.
     * 
     * @param filePart Parte del file
     * @return Contenuto del file come stringa
     * @throws IOException Se si verifica un errore durante la lettura
     */
    private static String readFileContent(Part filePart) throws IOException {
        try (InputStream fileContent = filePart.getInputStream()) {
            byte[] contentBytes = new byte[fileContent.available()];
            fileContent.read(contentBytes);
            return new String(contentBytes, StandardCharsets.UTF_8);
        }
    }
    
    /**
     * Sanitizza il contenuto HTML per prevenire attacchi XSS.
     * 
     * @param content Contenuto HTML grezzo
     * @return Contenuto HTML sanitizzato
     */
    private static String sanitizeHtml(String content) {
    	
        // Usa Jsoup per sanitizzare l'HTML
        Document document = Jsoup.parse(content);
        
        // Rimuovi elementi pericolosi
        document.select("script, [type=application/javascript], [type=text/javascript]").remove();
        
        // Rimuovi attributi pericolosi
        document.select("[onclick], [onload], [onerror], [onfocus], [onblur]").forEach(element -> {
            element.removeAttr("onclick");
            element.removeAttr("onload");
            element.removeAttr("onerror");
            element.removeAttr("onfocus");
            element.removeAttr("onblur");
        });
        
        // Pulisci ulteriormente con Safelist
        String cleanedHtml = Jsoup.clean(document.body().html(), Safelist.relaxed());
        
        return cleanedHtml;
        
    }
    
    /**
     * Estrae l'estensione da un nome file.
     * 
     * @param fileName Nome del file
     * @return Estensione del file
     */
    private static String getFileExtension(String fileName) {
        int lastDotPos = fileName.lastIndexOf(".");
        if (lastDotPos > 0) {
            return fileName.substring(lastDotPos + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * Estrae il nome del file da un percorso completo.
     * 
     * @param filePath Percorso del file
     * @return Nome del file
     */
    private static String getFileNameFromPath(String filePath) {
        Path path = Paths.get(filePath);
        return path.getFileName().toString();
    }
}