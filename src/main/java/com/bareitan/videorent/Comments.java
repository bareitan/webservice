package com.bareitan.videorent;

/**
 * Created by bareitan on 31/03/2017.
 */
import com.google.gson.Gson;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@Path("/comments")
public class Comments {

    @GET
    @Path("{movieid}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getAllCommentsForMovie(@PathParam("movieid") String movieId) {
        Gson gson = new Gson();
        Connection connection;
        PreparedStatement selectComments;
        ArrayList<Comment> comments = new ArrayList<Comment>();
        String error = "";


        try {
            connection = DBConnection.createConnection();
            String select = "SELECT CommentText,Rating, FirstName,LastName FROM comments AS C left join users AS U " +
                    "ON C.UserID= U.UserID WHERE C.MovieID = ?";

            selectComments = connection.prepareStatement(select);
            selectComments.setString(1, movieId);

            ResultSet rs = selectComments.executeQuery();
            while (rs.next()) {
                comments.add(
                        new Comment(
                                rs.getString("CommentText"),
                                rs.getString("FirstName") + " " + rs.getString("LastName"),
                                rs.getInt("Rating")
                        ));
            }

        } catch (Exception e) {
            error = e.getLocalizedMessage();
        }
        HashMap hm = new HashMap();
        hm.put("Comments", comments);
        hm.put("error",error);
        return gson.toJson(hm);
    }

    @POST
    @Path("{movieid}/{userid}/{text}/{rating}")
    @Produces(MediaType.APPLICATION_JSON)
    public String addCommentToMovie(@PathParam("movieid") String movieId,
                                    @PathParam("userid") String userId,
                                    @PathParam("text") String text,
                                    @PathParam("rating") String rating)
    {
        Gson gson = new Gson();
        Connection connection = null;
        PreparedStatement insertComment = null;
        AddCommentResponse addCommentResponse = null;


        if (movieId != "" || userId != "" || text != "" || rating != "") {
            try {
                connection = DBConnection.createConnection();
                insertComment = connection.prepareStatement(
                        "INSERT INTO COMMENTS(UserID,MovieID,CommentText,Rating) " +
                                "VALUES (?,?,?,?)");

                insertComment.setString(1, userId);
                insertComment.setString(2, movieId);
                insertComment.setString(3, text);
                insertComment.setString(4, rating);

                int records = insertComment.executeUpdate();
                if (records > 0) {
                    addCommentResponse = new AddCommentResponse(true, "");
                } else {
                    addCommentResponse = new AddCommentResponse(false, "Couldn't add comment.");
                }
            }
            catch (Exception e) {
                addCommentResponse = new AddCommentResponse(false, e.getLocalizedMessage());
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            addCommentResponse = new AddCommentResponse(false, "Mandatory values were not provided.");
        }
        return gson.toJson(addCommentResponse);
    }

    public class Comment{

        String text;
        String userName;
        int rating;

        public Comment(String text, String userName, int rating) {
            this.text = text;
            this.userName = userName;
            this.rating = rating;
        }


    }
    class AddCommentResponse {
        public Boolean added;
        public String error;

        public AddCommentResponse(Boolean added, String error) {
            this.added = added;
            this.error = error;
        }
    }
}

