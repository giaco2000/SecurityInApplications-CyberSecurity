<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Proposte Progettuali - Applicazione Sicura</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            max-width: 800px;
            margin: 50px auto;
            padding: 20px;
            background-color: white;
            border-radius: 5px;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
        }
        h1, h2 {
            color: #333;
        }
        .back-link {
            display: block;
            margin-bottom: 20px;
        }
        .project-form {
            margin-bottom: 30px;
            padding: 20px;
            border: 1px solid #ddd;
            border-radius: 5px;
        }
        label {
            display: block;
            margin: 10px 0 5px;
            font-weight: bold;
        }
        input[type="file"] {
            width: 100%;
            padding: 10px;
            margin-bottom: 15px;
            border: 1px solid #ddd;
            border-radius: 4px;
            box-sizing: border-box;
        }
        button {
            background-color: #4CAF50;
            color: white;
            padding: 10px 15px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 16px;
        }
        button:hover {
            background-color: #45a049;
        }
        .project-list {
            margin-top: 30px;
        }
        .project {
            margin-bottom: 20px;
            padding: 15px;
            border: 1px solid #ddd;
            border-radius: 5px;
        }
        .project-header {
            display: flex;
            justify-content: space-between;
            margin-bottom: 10px;
            border-bottom: 1px solid #eee;
            padding-bottom: 10px;
        }
        .project-content {
            margin-top: 15px;
            padding: 10px;
            background-color: #f9f9f9;
            border-radius: 4px;
            max-height: 300px;
            overflow-y: auto;
        }
        .no-projects {
            text-align: center;
            margin: 30px 0;
            color: #666;
        }
        .message {
            padding: 10px;
            margin-bottom: 15px;
            border-radius: 4px;
        }
        .success {
            background-color: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        .error {
            background-color: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
    </style>
    
    <!-- Controllo autenticazione -->
    <script>
    document.addEventListener("DOMContentLoaded", function() {
        // Controllo tramite sessione (più affidabile dell'attributo di richiesta)
        <% 
        HttpSession userSession = request.getSession(false);
        boolean isAuthenticated = (userSession != null && userSession.getAttribute("nomeUtente") != null);
        %>
        var isLoggedIn = <%= isAuthenticated %>;
        if (!isLoggedIn) {
            window.location.href = "login.jsp";
        }
    });
</script>
</head>
<body>
    <div class="container">
        <a href="benvenuto.jsp" class="back-link">← Torna alla pagina principale</a>
        
        <h1>Proposte Progettuali</h1>
        
        <% 
        String action = request.getParameter("action");
        if (action != null && action.equals("upload")) { 
        %>
            <div class="project-form">
                <h2>Carica una Nuova Proposta</h2>
                
                <% if (request.getAttribute("success") != null) { %>
                    <div class="message success">
                        <%= request.getAttribute("success") %>
                    </div>
                <% } %>
                
                <% if (request.getAttribute("error") != null) { %>
                    <div class="message error">
                        <%= request.getAttribute("error") %>
                    </div>
                <% } %>
                
                <form action="ProjectServlet" method="post" enctype="multipart/form-data" onsubmit="return validateUpload()">
    				<input type="hidden" name="nomeUtente" 
           				value="${sessionScope.nomeUtente != null ? sessionScope.nomeUtente : param.nomeUtente}">
                    <label for="propostaFile">File della proposta (solo .txt):</label>
                    <input type="file" id="propostaFile" name="Proposta progettuale" accept=".txt" required>
                    <button type="submit">Carica Proposta</button>
                </form>
            </div>
        <% } %>
        
        <div class="project-list">
            <h2>Elenco delle Proposte</h2>
            <div id="projects-container">
                <!-- Qui verranno caricate le proposte dal server -->
                <p class="no-projects">Caricamento proposte in corso...</p>
            </div>
        </div>
    </div>
    
    <script>
        // Validazione del form di upload
        function validateUpload() {
            var fileInput = document.getElementById("propostaFile");
            if (fileInput.files.length > 0) {
                var fileName = fileInput.files[0].name;
                var fileExt = fileName.split('.').pop().toLowerCase();
                
                if (fileExt !== 'txt') {
                    alert("Formato file non supportato. Utilizzare solo file .txt");
                    return false;
                }
            }
            return true;
        }
        
        // Funzione di escape per prevenire XSS
        function escapeHtml(unsafe) {
            return unsafe
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/"/g, "&quot;")
                .replace(/'/g, "&#039;");
        
        }
        
        // Carica le proposte progettuali con XMLHttpRequest
        window.onload = function() {
            var xhr = new XMLHttpRequest();
            
            xhr.onreadystatechange = function() {
                if (xhr.readyState === 4) {
                    if (xhr.status === 200) {
                        try {
                            console.log("Risposta ricevuta:", xhr.responseText);
                            var data = JSON.parse(xhr.responseText);
                            
                            const container = document.getElementById('projects-container');
                            container.innerHTML = '';
                            
                            if (data.length === 0) {
                                container.innerHTML = '<p class="no-projects">Nessuna proposta trovata</p>';
                                return;
                            }
                            
                            data.forEach(project => {
                                const projectDiv = document.createElement('div');
                                projectDiv.className = 'project';
                                
                                const header = document.createElement('div');
                                header.className = 'project-header';
                                header.innerHTML = `
                                    <div><strong>Utente:</strong> \${escapeHtml(project.username)}</div>
                                    <div><strong>File:</strong> \${escapeHtml(project.fileName)}</div>
                                `;
                                
                                const content = document.createElement('div');
                                content.className = 'project-content';
                                content.innerHTML = project.htmlContent;
                                
                                projectDiv.appendChild(header);
                                projectDiv.appendChild(content);
                                container.appendChild(projectDiv);
                            });
                        } catch (e) {
                            console.error("Errore parsing JSON:", e);
                            document.getElementById('projects-container').innerHTML = 
                                '<p class="no-projects">Errore nel caricamento delle proposte</p>';
                        }
                    } else {
                        console.error("Errore HTTP:", xhr.status);
                        document.getElementById('projects-container').innerHTML = 
                            '<p class="no-projects">Errore nel caricamento delle proposte</p>';
                    }
                }
            };
            
            xhr.open('GET', 'ProjectServlet', true);
            xhr.setRequestHeader('Content-Type', 'application/json');
            xhr.send();
        };
    </script>
    <!-- script per il timeout della sessione -->
    <script>
        // Timeout di 15 minuti in millisecondi
        var sessionTimeoutMillis = 900000;

     // Funzione da eseguire quando la sessione scade
        function sessionExpired() {
            // Chiamata sincrona al LogoutServlet per assicurarsi che la sessione sia invalidata
            // prima di procedere con l'alert e il reindirizzamento
            var xhr = new XMLHttpRequest();
            xhr.open('GET', 'LogoutServlet?timeout=true', false); // Sincrona!
            xhr.send();
            
            // Mostra l'alert
            alert("La tua sessione è scaduta per inattività.");
            
            // Reindirizza alla pagina di login
            window.location.href = 'login.jsp?timeout=true';
        }

        // Imposta il timer di timeout
        var sessionTimer = setTimeout(sessionExpired, sessionTimeoutMillis);

        // Resetta il timer quando l'utente interagisce con la pagina
        function resetTimer() {
            clearTimeout(sessionTimer);
            sessionTimer = setTimeout(sessionExpired, sessionTimeoutMillis);
        }
    </script>
</body>
</html>