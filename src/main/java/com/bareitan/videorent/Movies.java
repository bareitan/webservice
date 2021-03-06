package com.bareitan.videorent;

/**
 * Created by bareitan on 31/03/2017.
 */
import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;



@Path("/movies")
public class Movies {


    @GET
    @Path("/getMovie")
    @Produces(MediaType.APPLICATION_JSON)
    public String getMovie(@QueryParam("movieID") Integer movieId) throws SQLException {

        Gson gson = new Gson();
        Connection connection =null;
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
        }finally {
            if (connection != null) {
                connection.close();
            }
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
    @Path("/getMovieByCategory/{category}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getMovieByCategory(@PathParam("category") String category) throws SQLException {
        Gson gson = new Gson();
        Connection connection = null;
        PreparedStatement selectCategory;
        ArrayList<Movie> movies = new ArrayList<Movie>();
        String error = "";


        try {
            connection = DBConnection.createConnection();
            String select = "SELECT * FROM movies AS M " +
                    "LEFT JOIN categories AS C ON M.CategoryID=C.CategoryID";
            if(!category.equals("All"))
            {
                select +=" WHERE C.CategoryName=?";
                selectCategory = connection.prepareStatement(select);
                selectCategory.setString(1, category);

            }else{
                selectCategory = connection.prepareStatement(select);
            }



            ResultSet rs = selectCategory.executeQuery();
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
            error = e.getLocalizedMessage();
        }finally {
            if (connection != null) {
                connection.close();
            }
        }
            HashMap hm = new HashMap();
            hm.put("Movies", movies);
            hm.put("error",error);
            return gson.toJson(hm);
    }



    @GET
    @Path("/addMovie")
    @Produces(MediaType.APPLICATION_JSON)
    public String addMovie(@QueryParam("movieName") String movieName,
                           @QueryParam("overview") String overview,
                           @QueryParam("stock") Integer stock,
                           @QueryParam("thumbnail") String thumbnail,
                           @QueryParam("categoryName") String categoryName) throws SQLException {
        Gson gson = new Gson();
        Connection connection = null;
        PreparedStatement insertMovie;
        PreparedStatement selectCategory;
        PreparedStatement insertCategory;
        MovieResponse movieResponse;
        int categoryId = -1;
        try {
            connection = DBConnection.createConnection();

            selectCategory = connection.prepareStatement(
                    "SELECT CategoryID from categories where CategoryName LIKE ?");
            selectCategory.setString(1, categoryName);


            ResultSet rs = selectCategory.executeQuery();
            if(rs.next())
            {
                categoryId= rs.getInt("CategoryID");
            }

            if(categoryId==-1)
            {
                insertCategory = connection.prepareStatement(
                        "INSERT INTO categories(CategoryName)" +
                                "VALUES (?)", Statement.RETURN_GENERATED_KEYS);
                insertCategory.setString(1, categoryName);

                int records = insertCategory.executeUpdate();
                if (records > 0) {
                    ResultSet keysSet = insertCategory.getGeneratedKeys();
                    keysSet.next();
                    categoryId = keysSet.getInt(1);
                }
            }


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
                movieResponse = new MovieResponse(true, "");
            } else {
                movieResponse = new MovieResponse(true, "Couldn't add movie.");
            }
        }catch (Exception e) {
            movieResponse = new MovieResponse(true, e.getLocalizedMessage());
        }finally {
            if (connection != null) {
                connection.close();
            }
        }
        return gson.toJson(movieResponse);
    }


    @GET
    @Path("/updateMovie")
    @Produces(MediaType.APPLICATION_JSON)
    public String updateMovie( @QueryParam("movieId") String movieId,
                            @QueryParam("movieName") String movieName,
                           @QueryParam("overview") String overview,
                           @QueryParam("stock") Integer stock,
                           @QueryParam("thumbnail") String thumbnail,
                           @QueryParam("categoryName") String categoryName) throws SQLException {
        Gson gson = new Gson();
        Connection connection = null;
        PreparedStatement updateMovie;
        PreparedStatement selectCategory;
        PreparedStatement insertCategory;
        PreparedStatement stockSelect;
        MovieResponse movieResponse;
        String error="";
        int categoryId = -1;
        try {
            connection = DBConnection.createConnection();

            selectCategory = connection.prepareStatement(
                    "SELECT CategoryID from categories where CategoryName LIKE ?");
            selectCategory.setString(1, categoryName);


            ResultSet rs = selectCategory.executeQuery();
            if(rs.next())
            {
                categoryId= rs.getInt("CategoryID");
            }

            if(categoryId==-1)
            {
                insertCategory = connection.prepareStatement(
                        "INSERT INTO categories(CategoryName)" +
                                "VALUES (?)", Statement.RETURN_GENERATED_KEYS);
                insertCategory.setString(1, categoryName);

                int records = insertCategory.executeUpdate();
                if (records > 0) {
                    ResultSet keysSet = insertCategory.getGeneratedKeys();
                    keysSet.next();
                    categoryId = keysSet.getInt(1);
                }
            }

            stockSelect = connection.prepareStatement("" +
                    "SELECT COUNT(*) FROM rents WHERE MovieID=? AND ReturnDate IS NULL");
            stockSelect.setString(1, movieId.toString());
            rs = stockSelect.executeQuery();
            int rentedMovies = -1;
            if (rs.next()) {
                rentedMovies = rs.getInt(1);
            } else {
                error = "Couldn't check the stock.";
            }
            if(rentedMovies!=-1 && rentedMovies>stock)
            {
                error="New stock is below the number of rented movies. ( " + rentedMovies + ")";
            }
            int records = -1;
            if(error=="") {
                updateMovie = connection.prepareStatement(
                        "UPDATE movies SET MovieName=?, Overview=?, Stock=?, CategoryID=?, Thumbnail=?" +
                                "WHERE MovieID=?");
                updateMovie.setString(1, movieName);
                updateMovie.setString(2, overview);

                updateMovie.setInt(3, stock);
                updateMovie.setInt(4, categoryId);
                updateMovie.setString(5, thumbnail);
                updateMovie.setString(6, movieId);

                records = updateMovie.executeUpdate();
            }
            if (records > 0) {
                movieResponse = new MovieResponse(true, "");
            } else {
                movieResponse = new MovieResponse(false, error);
            }
        }catch (Exception e) {
            e.printStackTrace();
            movieResponse = new MovieResponse(false, e.getLocalizedMessage());

        }finally {
            if (connection != null) {
                connection.close();
            }
        }
        return gson.toJson(movieResponse);
    }


    @GET
    @Path("/deleteMovie")
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteMovie(@QueryParam("movieId") String movieId) throws SQLException {
        Gson gson = new Gson();
        Connection connection = null;
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
        }finally {
            if (connection != null) {
                connection.close();
            }
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