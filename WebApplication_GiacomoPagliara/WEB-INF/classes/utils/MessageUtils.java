package utils;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * Classe di utilità per visualizzare messaggi all'utente.
 * Supporta diversi tipi di messaggi e modalità di visualizzazione.
 * 
 * @author Giacomo Pagliara
 */
public class MessageUtils {
    // Costante per il titolo della finestra
    private static final String WINDOW_TITLE = "MESSAGGIO DAL SITO WEB";
    
    /**
     * Visualizza un messaggio generico in una finestra separata.
     * 
     * @param message Messaggio da visualizzare
     */
    public static void showMessage(String message) {
        showCustomPanel(message);
    }
    
    /**
     * Visualizza un messaggio di errore.
     * 
     * @param message Messaggio di errore da visualizzare
     */
    public static void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "Errore", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Visualizza un messaggio informativo.
     * 
     * @param message Messaggio informativo da visualizzare
     */
    public static void showInfoMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "Informazione", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Visualizza un messaggio di avviso.
     * 
     * @param message Messaggio di avviso da visualizzare
     */
    public static void showWarningMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "Attenzione", JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * Visualizza un messaggio in un pannello personalizzato.
     *
     * 
     * @param message Messaggio da visualizzare
     */
    private static void showCustomPanel(String message) {
        // Crea una finestra e aggiungi il pannello
        JFrame frame = new JFrame(WINDOW_TITLE);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = createMessagePanel(message);
        frame.getContentPane().add(panel);
        
        frame.pack(); 
        frame.setLocationRelativeTo(null);
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);
    }
    
    /**
     * Crea un pannello contenente il messaggio.
     * 
     * @param message Messaggio da visualizzare
     * @return Pannello configurato
     */
    private static JPanel createMessagePanel(String message) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JLabel label = new JLabel(message);
        panel.add(label, BorderLayout.CENTER);
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        return panel;
    }
}