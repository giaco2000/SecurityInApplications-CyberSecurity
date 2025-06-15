package project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.DatabaseConnection;
import query.DatabaseQueries;

/**
 * Modello che rappresenta una proposta progettuale.
 * Contiene informazioni sul proprietario, nome del file e contenuto.
 * 
 * @author Giacomo Pagliara
 */
public class Project {
    private String username;
    private String fileName;
    private String htmlContent;

    /**
     * Costruttore per creare una nuova proposta progettuale.
     * 
     * @param username Nome utente del proprietario
     * @param fileName Nome del file della proposta
     * @param htmlContent Contenuto HTML della proposta
     */
    public Project(String username, String fileName, String htmlContent) {
        this.username = username;
        this.fileName = fileName;
        this.htmlContent = htmlContent;
    }

    /**
     * Recupera tutte le proposte progettuali dal database.
     * 
     * @return Lista di tutte le proposte progettuali
     */
    public static List<Project> getAllProjects() {
        List<Project> projects = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnectionRead();
             PreparedStatement preparedStatement = connection.prepareStatement(DatabaseQueries.getUsersAndProposalsQuery());
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String fileName = resultSet.getString("fileName");
                String htmlContent = resultSet.getString("htmlContent");
               
                Project project = new Project(username, fileName, htmlContent);
                projects.add(project);
            }
            System.out.println("Trovate " + projects.size() + " proposte"); // Debug

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return projects;
    }
    
    /**
     * Ottiene il nome utente del proprietario della proposta.
     * 
     * @return Nome utente del proprietario
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Ottiene il contenuto HTML della proposta.
     * 
     * @return Contenuto HTML
     */
    public String getHtmlContent() {
        return htmlContent;
    }

    /**
     * Ottiene il nome del file della proposta.
     * 
     * @return Nome del file
     */
    public String getFileName() {
        return fileName;
    }
}