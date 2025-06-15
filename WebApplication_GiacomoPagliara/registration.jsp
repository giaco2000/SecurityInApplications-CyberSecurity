<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registrazione - Applicazione Sicura</title>
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
        input[type="text"], input[type="password"], input[type="file"] {
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
            width: 100%;
        }
        button:hover {
            background-color: #45a049;
        }
        .login-link {
            text-align: center;
            margin-top: 20px;
        }
        .password-requirements {
            font-size: 12px;
            color: #666;
            margin-top: 5px;
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
        <h1>Registrazione</h1>
        
        <% if (request.getAttribute("error") != null) { %>
            <div class="message error">
                <%= request.getAttribute("error") %>
            </div>
        <% } %>
        
        <form action="RegistrationServlet" method="post" enctype="multipart/form-data" onsubmit="return validateForm()">
            <label for="username">Nome utente:</label>
            <input type="text" id="username" name="username" required pattern="^[a-zA-Z0-9]+$" 
                   title="Solo lettere e numeri sono consentiti" maxlength="45">
            
            <label for="password">Password:</label>
            <input type="password" id="password" name="password" required minlength="8">
            <div class="password-requirements">
                La password deve contenere almeno 8 caratteri, includere una lettera maiuscola,
                un numero e un carattere speciale.
            </div>
            
            <label for="conferma_password">Conferma password:</label>
            <input type="password" id="conferma_password" name="conferma_password" required>
            
            <label for="ImmagineProfilo">Immagine profilo:</label>
            <input type="file" id="ImmagineProfilo" name="ImmagineProfilo" accept=".jpg,.jpeg,.png" required>
            <div class="password-requirements">
                Formati accettati: JPG, JPEG, PNG. Dimensione massima: 5MB.
            </div>
            
            <button type="submit">Registrati</button>
        </form>
        
        <div class="login-link">
            Hai già un account? <a href="login.jsp">Accedi</a>
        </div>
    </div>

    <script>
        function validateForm() {
            var password = document.getElementById("password").value;
            var confirmPassword = document.getElementById("conferma_password").value;
            
            // Verifica che la password soddisfi i requisiti
            var hasUpperCase = /[A-Z]/.test(password);
            var hasDigit = /\d/.test(password);
            var hasSpecial = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password);
            
            if (!hasUpperCase || !hasDigit || !hasSpecial) {
                alert("La password deve contenere almeno una lettera maiuscola, un numero e un carattere speciale.");
                return false;
            }
            
            // Verifica che le password corrispondano
            if (password !== confirmPassword) {
                alert("Le password non corrispondono.");
                return false;
            }
            
            // Verifica il file caricato
            var fileInput = document.getElementById("ImmagineProfilo");
            if (fileInput.files.length > 0) {
                var fileSize = fileInput.files[0].size; // in bytes
                var fileName = fileInput.files[0].name;
                var fileExt = fileName.split('.').pop().toLowerCase();
                
                if (fileSize > 5 * 1024 * 1024) { // 5MB
                    alert("Il file è troppo grande. La dimensione massima è 5MB.");
                    return false;
                }
                
                if (!['jpg', 'jpeg', 'png'].includes(fileExt)) {
                    alert("Formato file non supportato. Utilizzare JPG, JPEG o PNG.");
                    return false;
                }
            }
            
            return true;
        }
    </script>
</body>
</html>