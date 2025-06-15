<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Benvenuto - Applicazione Sicura</title>
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
        h1 {
            text-align: center;
            color: #333;
        }
        .welcome-message {
            text-align: center;
            margin-bottom: 30px;
        }
        .actions {
            display: flex;
            justify-content: space-around;
            margin-top: 40px;
        }
        .action-button {
            display: inline-block;
            background-color: #4CAF50;
            color: white;
            padding: 15px 25px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 16px;
            text-decoration: none;
            text-align: center;
        }
        .action-button:hover {
            background-color: #45a049;
        }
        .logout {
            text-align: center;
            margin-top: 40px;
        }
        .logout-button {
            background-color: #f44336;
            color: white;
            padding: 10px 15px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 16px;
            text-decoration: none;
        }
        .logout-button:hover {
            background-color: #d32f2f;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Benvenuto nell'Applicazione Sicura</h1>
        
        <div class="welcome-message">
	    <% if (session.getAttribute("nomeUtente") != null) { %>
	        <h2>Ciao, <%= session.getAttribute("nomeUtente") %>!</h2>
	    <% } else { %>
	        <h2>Accesso effettuato con successo!</h2>
	    <% } %>
	    <p>Cosa desideri fare oggi?</p>
		</div>
	
		<div class="actions">
		    <a href="progetti.jsp?action=view&nomeUtente=${sessionScope.nomeUtente}" class="action-button">Visualizza Proposte</a>
		    <a href="progetti.jsp?action=upload&nomeUtente=${sessionScope.nomeUtente}" class="action-button">Carica Nuova Proposta</a>
		</div>
	        
        <div class="logout">
            <a href="LogoutServlet" class="logout-button">Logout</a>
        </div>
    </div>
    
    <script>
        // Se l'utente accede alla pagina di benvenuto senza essere loggato, reindirizza al login
        window.onload = function() {
            <% 
			HttpSession userSession = request.getSession(false);
			if (userSession == null || userSession.getAttribute("login") == null || !(Boolean)userSession.getAttribute("login")) {
			    response.sendRedirect("login.jsp");
			    return;
			}
			%>
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