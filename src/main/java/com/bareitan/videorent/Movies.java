package com.bareitan.videorent;

/**
 * Created by bareitan on 31/03/2017.
 */
import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.*;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;



@Path("/movies")
public class Movies {

    @GET
    @Path("/getAllMovies")
    @Produces(MediaType.APPLICATION_JSON)
    public String getAllMovies() {

        Gson gson = new Gson();
        Connection connection = null;
        PreparedStatement getAllStatement = null;
        ArrayList<Movie> movies = new ArrayList<Movie>();

        try {
            connection = DBConnection.createConnection();
            getAllStatement = connection.prepareStatement("SELECT * FROM movies");
            ResultSet rs = getAllStatement.executeQuery();
            while (rs.next()) {
                movies.add(
                        new Movie(
                                rs.getInt("MovieID"),
                                rs.getString("MovieName"),
                                rs.getInt("CategoryID"))
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (movies.size() > 0) {
            return gson.toJson(movies);
        } else {
            return gson.toJson("Couldn't fetch movies");
        }
    }


    @GET
    @Path("/addMovie")
    @Produces(MediaType.APPLICATION_JSON)
    public String addMovie(@QueryParam("movieName") String movieName,
                           @QueryParam("categoryId") Integer categoryId) {

        Gson gson = new Gson();
        Connection connection = null;
        PreparedStatement insertMovie = null;
        try {
            connection = DBConnection.createConnection();
            insertMovie = connection.prepareStatement("INSERT INTO movies(MovieName,CategoryID)" +
                    "VALUES (?,?)");
            insertMovie.setString(1, movieName);
            insertMovie.setInt(2, categoryId);

            int records = insertMovie.executeUpdate();
            if (records > 0) {
                return gson.toJson("Movie was added successfully.");
            } else {
                return gson.toJson("Couldn't add movie");
            }
        }catch (Exception e) {
            e.printStackTrace();
            return gson.toJson("Couldn't add movie");
        }
    }
}

class Movie {
    int movieID;
    String movieName;
    int categoryID;
    public Movie (int movieID, String movieName,int categoryId) {
        this.movieID = movieID;
        this.movieName = movieName;
        this.categoryID = categoryId;
    }
}