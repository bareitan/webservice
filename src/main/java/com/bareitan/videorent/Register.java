package com.bareitan.videorent;

/**
 * Created by bareitan on 31/03/2017.
 */
import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
//Path: http://localhost/<appln-folder-name>/register
@Path("/register")
public class Register {

    @GET
    @Path("/doRegister")
    @Produces(MediaType.APPLICATION_JSON)
    public String doLogin(@QueryParam("firstName") String firstName,
                          @QueryParam("lastName") String lastName,
                          @QueryParam("email") String email,
                          @QueryParam("isAdmin") Integer isAdmin,
                          @QueryParam("password") String password) throws SQLException {

        Gson gson = new Gson();
        Connection connection = null;
        PreparedStatement insertUser = null;
        RegisterResponse registerResponse = null;


        if (email != "" || password != "" || firstName != "" || lastName != "" || isAdmin != null) {
            try {
                connection = DBConnection.createConnection();
                insertUser = connection.prepareStatement(
                        "INSERT INTO users(FirstName,LastName,Email,Password,IsAdmin) " +
                                "VALUES (?,?,?,?,?)");

                insertUser.setString(1, firstName);
                insertUser.setString(2, lastName);
                insertUser.setString(3, email);
                insertUser.setString(4, password);
                insertUser.setInt(5, isAdmin);
                int records = insertUser.executeUpdate();
                if (records > 0) {
                    registerResponse = new RegisterResponse(true, "");
                } else {
                    registerResponse = new RegisterResponse(false, "Couldn't add user.");
                }
            } catch(SQLException e){
                if(e.getErrorCode() == 1062) {
                    registerResponse = new RegisterResponse(false, "User already exists");
                }
            }
            catch (Exception e) {
                registerResponse = new RegisterResponse(false, e.toString());
            } finally {
                if (connection != null) {
                    connection.close();
                }
            }
        } else {
            registerResponse = new RegisterResponse(false, "Mandatory values were not provided.");
        }
        return gson.toJson(registerResponse);
    }
}

    class RegisterResponse {
        public Boolean registeredSuccessfully;
        public String error;
        public RegisterResponse(Boolean registeredSuccessfully,String error){
            this.registeredSuccessfully = registeredSuccessfully;
            this.error = error;
        }
    }