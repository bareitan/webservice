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



@Path("/comments")
public class Comments {

    @GET
    @Path("{movieid}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getAllCommentsForMovie(@PathParam("movieid") String movieId) throws SQLException {
        Gson gson = new Gson();
        Connection connection = null;
        PreparedStatement selectComments;
        ArrayList<Comment> comments = new ArrayList<Comment>();
        String error = "";


        try {
            connection = DBConnection.createConnection();
            String select = "SELECT CommentText,Rating, FirstName,LastName, C.UserID, CommentID FROM comments AS C left join users AS U " +
                    "ON C.UserID= U.UserID WHERE C.MovieID = ?" +
                    " ORDER BY CommentID DESC ";

            selectComments = connection.prepareStatement(select);
            selectComments.setString(1, movieId);

            ResultSet rs = selectComments.executeQuery();
            while (rs.next()) {
                comments.add(
                        new Comment(
                                rs.getString("CommentText"),
                                rs.getString("FirstName") + " " + rs.getString("LastName"),
                                rs.getInt("Rating"),
                                rs.getInt("UserID"),
                                rs.getInt("CommentID")
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
                                    @PathParam("rating") String rating) throws SQLException {
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
                    connection.close();
                }
            }
        } else {
            addCommentResponse = new AddCommentResponse(false, "Mandatory values were not provided.");
        }
        return gson.toJson(addCommentResponse);
    }


    @DELETE
    @Path("{commentid}")
    @Produces(MediaType.APPLICATION_JSON)
    public String addCommentToMovie(@PathParam("commentid") String commentId) throws SQLException {
        Gson gson = new Gson();
        Connection connection = null;
        PreparedStatement deleteComment = null;
        DeleteCommentResponse deleteCommentResponse = null;


        if (commentId != "") {
            try {
                connection = DBConnection.createConnection();
                deleteComment = connection.prepareStatement(
                        "DELETE FROM COMMENTS WHERE CommentID = ?");

                deleteComment.setString(1, commentId);

                int records = deleteComment.executeUpdate();
                if (records > 0) {
                    deleteCommentResponse = new DeleteCommentResponse(true, "");
                } else {
                    deleteCommentResponse = new DeleteCommentResponse(false, "Couldn't delete comment.");
                }
            }
            catch (Exception e) {
                deleteCommentResponse = new DeleteCommentResponse(false, e.getLocalizedMessage());
            } finally {
                if (connection != null) {
                    connection.close();
                }
            }
        } else {
            deleteCommentResponse = new DeleteCommentResponse(false, "Mandatory values were not provided.");
        }
        return gson.toJson(deleteCommentResponse);
    }


    public class Comment{

        String text;
        String userName;
        int userId;
        int rating;
        int commentId;

        public Comment(String text, String userName, int rating, int userId, int commentId) {
            this.text = text;
            this.userName = userName;
            this.rating = rating;
            this.userId = userId;
            this.commentId = commentId;
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

    class DeleteCommentResponse {
        public Boolean deleted;
        public String error;

        public DeleteCommentResponse(Boolean deleted, String error) {
            this.deleted = deleted;
            this.error = error;
        }
    }
}

