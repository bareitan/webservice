package com.bareitan.videorent;

/**
 * Created by bareitan on 31/03/2017.
 */
import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;



@Path("/movies")
public class Movies {


    @GET
    @Path("/getMovie")
    @Produces(MediaType.APPLICATION_JSON)
    public String getMovie(@QueryParam("movieID") Integer movieId) {

        Gson gson = new Gson();
        Connection connection;
        PreparedStatement getStatement;
        Movie movie =null;
        try {
            connection = DBConnection.createConnection();
            getStatement = connection.prepareStatement("SELECT * FROM movies natural join categories" +
                    " WHERE MovieID = ?");
            getStatement.setString(1, movieId.toString());
            ResultSet rs = getStatement.executeQuery();
            rs.next();
            movie = new Movie(
                    rs.getInt("MovieID"),
                    rs.getString("MovieName"),
                    rs.getString("Overview"),
                    rs.getInt("Stock"),
                    rs.getInt("CategoryID"),
                    rs.getString("CategoryName"),
                    rs.getString("Thumbnail")
                    );


        } catch (Exception e) {
            e.printStackTrace();
        }
        if (movie != null) {
            HashMap hm = new HashMap();
            hm.put("movie", movie);
            return gson.toJson(hm);
        } else {
            return gson.toJson("Couldn't fetch movies");
        }
    }



    @GET
    @Path("/getAllMovies")
    @Produces(MediaType.APPLICATION_JSON)
    public String getAllMovies() {

        Gson gson = new Gson();
        Connection connection;
        PreparedStatement getAllStatement;
        ArrayList<Movie> movies = new ArrayList<Movie>();


        try {
            connection = DBConnection.createConnection();
            getAllStatement = connection.prepareStatement("SELECT * FROM movies AS M " +
                    "LEFT JOIN categories AS C ON M.CategoryID=C.CategoryID");
            ResultSet rs = getAllStatement.executeQuery();
            while (rs.next()) {
                movies.add(
                        new Movie(
                                rs.getInt("MovieID"),
                                rs.getString("MovieName"),
                                rs.getString("Overview"),
                                rs.getInt("Stock"),
                                rs.getInt("CategoryID"),
                                rs.getString("CategoryName"),
                                rs.getString("Thumbnail")
                        ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (movies.size() > 0) {
            HashMap hm = new HashMap();
            hm.put("Movies", movies);
            return gson.toJson(hm);
        } else {
            return gson.toJson("Couldn't fetch movies");
        }
    }


    @GET
    @Path("/addMovie")
    @Produces(MediaType.APPLICATION_JSON)
    public String addMovie(@QueryParam("movieName") String movieName,
                           @QueryParam("overview") String overview,
                           @QueryParam("stock") Integer stock,
                           @QueryParam("thumbnail") String thumbnail,
                           @QueryParam("categoryId") Integer categoryId)
    {
        Gson gson = new Gson();
        Connection connection;
        PreparedStatement insertMovie;
        try {
            connection = DBConnection.createConnection();
            insertMovie = connection.prepareStatement(
                    "INSERT INTO movies(MovieName, Overview, Stock, CategoryID,Thumbnail)" +
                    "VALUES (?,?,?,?,?)");
            insertMovie.setString(1, movieName);
            insertMovie.setString(2, overview);

            insertMovie.setInt(3, stock);
            insertMovie.setInt(4, categoryId);
            insertMovie.setString(5, thumbnail);

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


    @GET
    @Path("/updateMovie")
    @Produces(MediaType.APPLICATION_JSON)
    public String updateMovie( @QueryParam("movieId") String movieId,
                            @QueryParam("movieName") String movieName,
                           @QueryParam("overview") String overview,
                           @QueryParam("stock") Integer stock,
                           @QueryParam("thumbnail") String thumbnail,
                           @QueryParam("categoryId") Integer categoryId)
    {
        Gson gson = new Gson();
        Connection connection;
        PreparedStatement updateMovie;
        MovieResponse movieResponse;
        try {
            connection = DBConnection.createConnection();
            updateMovie = connection.prepareStatement(
                    "UPDATE movies set MovieName=?, Overview=?, Stock=?, CategoryID=?, Thumbnail=?" +
                            "where MovieID=?" );
            updateMovie.setString(1, movieName);
            updateMovie.setString(2, overview);

            updateMovie.setInt(3, stock);
            updateMovie.setInt(4, categoryId);
            updateMovie.setString(5, thumbnail);
            updateMovie.setString(6, movieId);

            int records = updateMovie.executeUpdate();
            if (records > 0) {
                movieResponse = new MovieResponse(true, "");
            } else {
                movieResponse = new MovieResponse(false, "Couldn't find movie.");

            }
        }catch (Exception e) {
            e.printStackTrace();
            movieResponse = new MovieResponse(false, e.getLocalizedMessage());

        }
        return gson.toJson(movieResponse);
    }


    @GET
    @Path("/deleteMovie")
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteMovie(@QueryParam("movieId") String movieId)
    {
        Gson gson = new Gson();
        Connection connection;
        PreparedStatement deleteMovie;
        MovieResponse deleteResponse;
        try {
            connection = DBConnection.createConnection();
            deleteMovie = connection.prepareStatement(
                    "DELETE FROM movies where MovieID=?");
            deleteMovie.setString(1, movieId);

            deleteMovie.executeUpdate();
            int records = deleteMovie.getUpdateCount();
            if (records > 0) {
                deleteResponse = new MovieResponse(true, "");
            } else {
                deleteResponse = new MovieResponse(false, "Movie not found");
            }
        }catch (Exception e) {
            e.printStackTrace();
            deleteResponse = new MovieResponse(false, e.getLocalizedMessage());
        }
        return gson.toJson(deleteResponse);
    }


}

class Movie {
    int movieID;
    String movieName;
    String overview;
    String categoryName;

    int stock;
    int categoryID;
    String thumbnail;
    public void Movie(){

    }
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

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Movie (int movieID,
                  String movieName,
                  String overview,
                  int stock,
                  int categoryId,
                  String categoryName,
                  String thumbnail)
    {
        this.movieID = movieID;
        this.movieName = movieName;
        this.overview = overview;
        this.stock = stock;
        this.categoryID = categoryId;
        this.categoryName = categoryName;
        this.thumbnail = thumbnail;
    }
}

class MovieResponse {
    public Boolean operationStatus;
    public String error;
    public MovieResponse(Boolean operationStatus, String error){
        this.operationStatus = operationStatus;
        this.error = error;
    }
}