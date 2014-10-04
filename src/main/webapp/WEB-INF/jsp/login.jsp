<html>
<head>
<title>Login Page</title>
</head>
<body>
	<center>
	<% String message = request.getParameter("loginStatus"); %>
	<%	if(message != null && !message.isEmpty()) { %>
			<h3 style="color:red;"><%=message%> </h3>
	<%	} %>
	<br>
    <h2>Notificator Login Page</h2>
    <form action="${pageContext.request.contextPath}/jsp/j_spring_security_check" method="post">
        <table>
            <tr>
                <td>Username:</td>
                <td><input type='text' name='username' /></td>
            </tr>
            <tr>
                <td>Password:</td>
                <td><input type='password' name='password'></td>
            </tr>
            <tr>
                <td colspan='2'><input name="submit" type="submit" value="Submit"></td>
            </tr>
        </table>
    </form>
    </center>
</body>
</html>