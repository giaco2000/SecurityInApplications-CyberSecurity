<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - Applicazione Sicura</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            max-width: 500px;
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
        label {
            display: block;
            margin: 10px 0 5px;
            font-weight: bold;
        }
        input[type="text"], input[type="password"] {
            width: 100%;
            padding: 10px;
            margin-bottom: 15px;
            border: 1px solid #ddd;
            border-radius: 4px;
            box-sizing: border-box;
        }
        input[type="checkbox"] {
            margin-right: 10px;
        }
        button {
            background-color: #4CAF50;
            color: white;
            padding: 10px 15px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 16px;
            width: 100%;
        }
        button:hover {
            background-color: #45a049;
        }
        .register-link {
            text-align: center;
            margin-top: 20px;
        }
        .message {
            padding: 10px;
            margin-bottom: 15px;
            border-radius: 4px;
        }
        .error {
            background-color: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Accedi</h1>
        
        <% if (request.getAttribute("error") != null) { %>
            <div class="message error">
                <%= request.getAttribute("error") %>
            </div>
        <% } %>

        <form action="LoginServlet" method="post">
            <label for="username">Nome utente:</label>
            <input type="text" id="username" name="username" required 
                   value="<%= request.getAttribute("decryptedUsername") != null ? request.getAttribute("decryptedUsername") : "" %>"
                   autocomplete="username">
            
            <label for="password">Password:</label>
            <input type="password" id="password" name="password" required 
                   value="<%= request.getAttribute("decryptedPassword") != null ? request.getAttribute("decryptedPassword") : "" %>"
                   autocomplete="current-password">
            
            <label>
                <input type="checkbox" name="ricordami" value="true"> Ricordami
            </label>
            
            <button type="submit">Accedi</button>
        </form>
        
        <div class="register-link">
            Non hai un account? <a href="registration.jsp">Registrati</a>
        </div>
    </div>

	<script>
	if (window.location.pathname.endsWith('/WebApplication_GiacomoPagliara/')) {
	  // Rimuove lo slash finale per evitare potenziali doppi slash
	  const basePath = window.location.pathname.substring(0, window.location.pathname.length - 1);
	  history.replaceState(null, document.title, basePath + "/login.jsp");
	}
	</script>
	
    <!-- Script per verificare i cookie e compilare automaticamente i campi -->
    <script>
 	// Funzione per convertire Base64 in byte array
    function base64ToBytes(base64) {
        const binaryString = atob(base64);
        const len = binaryString.length;
        const bytes = new Uint8Array(len);
        for (let i = 0; i < len; i++) {
            bytes[i] = binaryString.charCodeAt(i);
        }
        return bytes;
    }

    // Funzione per convertire byte array in stringa leggibile
    function bytesToUtf8String(bytes) {
        return new TextDecoder().decode(bytes);
    }
        window.onload = function() {
            // Invia una richiesta GET a LoginServlet per verificare i cookie
            fetch('LoginServlet', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
            .then(response => response.json())
            .then(data => {
                console.log("Risposta del server:", data);
                
                if (data.cookiesPresent) {
                    console.log("Cookie presenti, username:", data.username);
                    document.getElementById('username').value = data.username;
                    
                    // Se l'utente è già autenticato, reindirizza
                    if (data.authenticated) {
                        window.location.href = "benvenuto.jsp";
                    }
                }
            })
            .catch(error => console.error('Error:', error));
        };
    </script>
</body>
</html>