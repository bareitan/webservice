package com.bareitan.videorent;

/**
 * Created by bareitan on 30/03/2017.
 */


        import javax.ws.rs.GET;
        import javax.ws.rs.Path;
        import javax.ws.rs.Produces;
        import javax.ws.rs.QueryParam;
        import javax.ws.rs.core.MediaType;
        import com.google.gson.*;
        import java.sql.*;


//Path: http://localhost/<appln-folder-name>/login
@Path("/login")
public class Login {
    // HTTP Get Method
    @GET
    // Path: http://localhost/<appln-folder-name>/login/dologin
    @Path("/dologin")
    // Produces JSON as response
    @Produces(MediaType.APPLICATION_JSON)
    // Query parameters are parameters: http://localhost/<appln-folder-name>/login/dologin?username=abc&password=xyz
    public String doLogin(@QueryParam("email") String email, @QueryParam("password") String password) {
        Gson gson = new Gson();
        Connection connection = null;
        PreparedStatement userLookup = null;
        LoginResponse loginResponse = null;
        int userId = -1;
        if(email != "" || password != "") {
            try {
                connection = DBConnection.createConnection();
                userLookup = connection.prepareStatement(
                        "SELECT * FROM users WHERE Email = ? AND Password = ?");
                userLookup.setString(1, email);
                userLookup.setString(2, password);

                ResultSet rs = userLookup.executeQuery();
                int count = 0;
                Boolean isAdmin = false;
                while (rs.next()) {
                    count++;
                    if (rs.getBoolean("isAdmin")) {
                        isAdmin = true;
                    }
                    userId = rs.getInt("UserID");
                }
                if (count == 1) {
                    loginResponse = new LoginResponse(true, isAdmin, "",userId);
                } else {
                    loginResponse = new LoginResponse(false, false, "Incorrect Email or Password",userId);
                }
            } catch (Exception e) {
                loginResponse = new LoginResponse(false, false, e.toString(),userId);
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else {
            loginResponse = new LoginResponse(false,false,"Email or password not specified",userId);
        }
        return gson.toJson(loginResponse);
    }


}

class LoginResponse {
    public Boolean loginSucceeded;
    public Boolean isAdmin;
    public String error;
    public int userID;
    public LoginResponse(Boolean loginSucceeded,Boolean isAdmin,String error, int userID){
        this.loginSucceeded = loginSucceeded;
        this.isAdmin = isAdmin;
        this.error = error;
        this.userID = userID;
    }
}