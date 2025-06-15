package registration;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.Part;
import org.apache.tika.Tika;

import utils.MessageUtils;

/**
 * Classe per la validazione dei file caricati dagli utenti.
 * Utilizza Apache Tika per verificare i tipi MIME dei file.
 *
 * @author Giacomo Pagliara
 */
public class FileValidator {

    // Costanti per i controlli sui file
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = new HashSet<>(
            Arrays.asList("jpeg", "jpg", "png"));
    private static final String IMAGE_MIME_PREFIX = "image/";

    /**
     * Verifica se un file caricato è un'immagine valida (jpg, jpeg, png).
     * Controlla estensione, dimensione e tipo MIME del file.
     *
     * @param filePart Parte del file caricato tramite multipart/form-data
     * @return true se il file è valido, false altrimenti
     * @throws IOException Se si verifica un errore durante la lettura del file
     */
    public static boolean isValidImageFile(Part filePart) throws IOException {
        // Controllo se il file esiste e non è vuoto
        if (filePart == null || filePart.getSize() <= 0) {
            MessageUtils.showErrorMessage("Nessun file caricato");
            return false;
        }
        
        String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        
        // Controllo dell'estensione del file
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(fileExtension)) {
            MessageUtils.showErrorMessage("Estensione del file non supportata. Formati accettati: jpg, jpeg, png");
            return false;
        }
        
        // Controllo della dimensione del file
        if (filePart.getSize() > MAX_IMAGE_SIZE) {
            MessageUtils.showErrorMessage("L'immagine selezionata supera la dimensione massima consentita di 5 MB");
            return false;
        }
        
        // Controllo del tipo MIME usando Apache Tika
        Tika tika = new Tika();
        String contentType = tika.detect(filePart.getInputStream());
        
        if (contentType == null || !contentType.startsWith(IMAGE_MIME_PREFIX)) {
            MessageUtils.showErrorMessage("Il file non è un'immagine valida");
            return false;
        }
        
        return true;
    }
    
    /**
     * Verifica se un file caricato è un documento di testo (.txt).
     * Controlla estensione e tipo MIME del file.
     *
     * @param filePart Parte del file caricato tramite multipart/form-data
     * @return true se il file è un documento di testo valido, false altrimenti
     * @throws IOException Se si verifica un errore durante la lettura del file
     */
    public static boolean isValidTextFile(Part filePart) throws IOException {
        // Implementazione per i file di testo (per la parte delle proposte progettuali)
        // Simile alla validazione dell'immagine ma per file .txt
        
        if (filePart == null || filePart.getSize() <= 0) {
            MessageUtils.showErrorMessage("Nessun file caricato");
            return false;
        }
        
        String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        
        if (!"txt".equals(fileExtension)) {
            MessageUtils.showErrorMessage("Il file deve essere in formato .txt");
            return false;
        }
        
        // Controllo del tipo MIME
        Tika tika = new Tika();
        String contentType = tika.detect(filePart.getInputStream());
        
        if (contentType == null || !contentType.contains("text/plain")) {
            MessageUtils.showErrorMessage("Il file non è un documento di testo valido");
            return false;
        }
        
        return true;
    }
}