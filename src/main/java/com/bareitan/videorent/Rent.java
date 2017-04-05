package com.bareitan.videorent;

import com.google.gson.Gson;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.json.simple.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by bareitan on 05/04/2017.
 */
@Path("/rent")
public class Rent {
    @GET
    @Path("/take")
    @Produces(MediaType.APPLICATION_JSON)
    public String takeMovie(
            @QueryParam("movieID") Integer movieId,
            @QueryParam("userID") Integer userId)
    {

        Connection connection;
        PreparedStatement insertRent;
        PreparedStatement selectRent;
        PreparedStatement stockSelect;
        Boolean rented = false;
        String error="";
        ResultSet rs;
        int rentID = -1;
        try {
            connection = DBConnection.createConnection();
            //Checks if the movie exists in stock
            stockSelect = connection.prepareStatement("" +
                    "SELECT " +
                    "  (SELECT Stock FROM movies WHERE MovieID=?) " +
                    "  - (SELECT COUNT(*) FROM rents WHERE MovieID=? AND  ReturnDate IS NULL) AS Difference");
            stockSelect.setString(1, movieId.toString());
            stockSelect.setString(2, movieId.toString());
            rs = stockSelect.executeQuery();

            int leftInStock = 0;
            if (rs.next()) {
                leftInStock = rs.getInt(1);
            }else {
                error="Couldn't check if the movie is in stock.";
            }
            if(leftInStock>0) {
               //Check if the user already rented this movie.
                selectRent = connection.prepareStatement(
                        "SELECT count(*) FROM rents " +
                                "WHERE MovieID=? AND UserID=? AND ReturnDate IS NULL"
                );

                selectRent.setString(1, movieId.toString());
                selectRent.setString(2, userId.toString());


                rs = selectRent.executeQuery();
                int numberOfRows = 0;

                if (rs.next()) {
                    numberOfRows = rs.getInt(1);
                    if (numberOfRows == 0) {
                        //insert the new rent
                        insertRent = connection.prepareStatement(
                                "INSERT INTO rents(UserID, MovieID, RentDate)" +
                                        "VALUES (?,?,now())", Statement.RETURN_GENERATED_KEYS);
                        insertRent.setString(1, userId.toString());
                        insertRent.setString(2, movieId.toString());

                        int records = insertRent.executeUpdate();
                        if (records > 0) {
                            rented = true;
                            ResultSet keysSet = insertRent.getGeneratedKeys();
                            keysSet.next();
                            rentID = keysSet.getInt(1);

                        } else {
                            rented = false;
                        }
                    } else {
                        error = "The movie is already rented by this user.";
                    }
                } else {
                    error = "error: could not get the record counts";
                }
            }else {
                error="Movie is not in stock.";
            }
        }
        catch (SQLException e) {
            if(e.getErrorCode()==1452)
            {
                error="User id or movie id does not exist.";
            }
            else {
                error = e.getLocalizedMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject response = new JSONObject();
        response.put("rented", rented);
        response.put("error", error);
        response.put("rentID", rentID);
        return response.toJSONString();
    }

    @GET
    @Path("/return")
    @Produces(MediaType.APPLICATION_JSON)
    public String returnMovie(
            @QueryParam("rentId") Integer rentId)
    {

        Connection connection;
        PreparedStatement updateReturn;
        Boolean returned = false;
        String error = "";
        try {
            connection = DBConnection.createConnection();
            updateReturn = connection.prepareStatement(
                    "UPDATE rents SET ReturnDate=now() WHERE RentID=?");
            updateReturn.setString(1, rentId.toString());

            int records = updateReturn.executeUpdate();
            if (records > 0) {
                returned = true;
            } else {
                returned = false;
            }
            connection.close();
        }catch (Exception e) {
            error = e.getLocalizedMessage();
        }

        JSONObject response = new JSONObject();
        response.put("returned", returned);
        response.put("error", error);
        return response.toJSONString();
    }


    @GET
    @Path("/checkStock")
    @Produces(MediaType.APPLICATION_JSON)
    public String takeMovie(
            @QueryParam("movieID") Integer movieId) {

        Connection connection;
        PreparedStatement stockSelect;
        String error = "";
        ResultSet rs;
        int stock = -1;
        try {
            connection = DBConnection.createConnection();
            //Checks if the movie exists in stock
            stockSelect = connection.prepareStatement("" +
                    "SELECT " +
                    "  (SELECT Stock FROM movies WHERE MovieID=?) " +
                    "  - (SELECT COUNT(*) FROM rents WHERE MovieID=? AND  ReturnDate IS NULL) AS Difference");
            stockSelect.setString(1, movieId.toString());
            stockSelect.setString(2, movieId.toString());
            rs = stockSelect.executeQuery();

            if (rs.next()) {
                stock = rs.getInt(1);
            } else {
                error = "Couldn't check if the movie is in stock.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject response = new JSONObject();
        response.put("movieID", movieId);
        response.put("stock", stock);
        response.put("error", error);
        return response.toJSONString();
    }

    @GET
    @Path("/usermovierent")
    @Produces(MediaType.APPLICATION_JSON)
    public String userMovieRent(
            @QueryParam("movieID") Integer movieId,
            @QueryParam("userID") Integer userID) {

        Connection connection;
        PreparedStatement rentedSelect;
        String error = "";
        ResultSet rs;
        Boolean isRented = false;
        int rentID = -1;
        try {
            connection = DBConnection.createConnection();

            rentedSelect = connection.prepareStatement("" +
                    "SELECT RentID FROM rents WHERE MovieID=? AND UserID=? AND ReturnDate IS NULL");
            rentedSelect.setString(1, movieId.toString());
            rentedSelect.setString(2, userID.toString());
            rs = rentedSelect.executeQuery();

            if(rs.next())
            {
                rentID= rs.getInt("RentID");
                isRented = true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject response = new JSONObject();
        response.put("isRented", isRented);
        response.put("rentID", rentID);
        response.put("error", error);
        return response.toJSONString();
    }




    @GET
    @Path("/userRents")
    @Produces(MediaType.APPLICATION_JSON)
    public String userRents(@QueryParam("userID") Integer userId)
    {
        Connection connection;
        PreparedStatement selectRent;
        String error="";
        ResultSet rs;
        ArrayList<RentItem> rentItems = new ArrayList<RentItem>();
        Gson gson = new Gson();

        try {
            connection = DBConnection.createConnection();
            selectRent = connection.prepareStatement("" +
                    "SELECT * FROM rents NATURAL JOIN users NATURAL JOIN movies " +
                    "WHERE userID=?" +
                    "ORDER BY RentDate");
            selectRent.setString(1, userId.toString());
            rs = selectRent.executeQuery();
            RentItem item;
            while (rs.next()) {
                item = new RentItem();

                item.setUserId(rs.getInt("UserID"));
                item.setUserEmail(rs.getString("Email"));
                item.setRentId(rs.getInt("RentID"));
                item.setRentDate(rs.getDate("RentDate"));
                item.setReturnDate(rs.getDate("ReturnDate"));
                item.setMovieID(rs.getInt("MovieID"));
                item.setMovieName(rs.getString("MovieName"));
                rentItems.add(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (rentItems.size() > 0) {
            HashMap hm = new HashMap();
            hm.put("RentItems", rentItems);
            return gson.toJson(hm);
        } else {
            return gson.toJson("Couldn't fetch rents.");
        }
    }


    public class RentItem{


        private int rentId;
        private int userId;
        private String userEmail;
        private Date rentDate;
        private Date returnDate;
        private int movieID;
        private String movieName;

        public int getMovieID() {
            return movieID;
        }

        public void setMovieID(int movieID) {
            this.movieID = movieID;
        }

        public String getMovieName() {
            return movieName;
        }

        public void setMovieName(String movieName) {
            this.movieName = movieName;
        }



        public int getRentId() {
            return rentId;
        }

        public void setRentId(int rentId) {
            this.rentId = rentId;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getUserEmail() {
            return userEmail;
        }

        public void setUserEmail(String userEmail) {
            this.userEmail = userEmail;
        }

        public Date getRentDate() {
            return rentDate;
        }

        public void setRentDate(Date rentDate) {
            this.rentDate = rentDate;
        }

        public Date getReturnDate() {
            return returnDate;
        }

        public void setReturnDate(Date returnDate) {
            this.returnDate = returnDate;
        }
    }
}
