package com.bareitan.videorent;

/**
 * Created by bareitan on 05/04/2017.
 */

import com.google.gson.Gson;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.simple.JSONObject;



@Path("/categories")
public class Categories {




    @GET
    @Path("/getAllCategories")
    @Produces(MediaType.APPLICATION_JSON)
    public String getAllCategories() throws SQLException {
        Gson gson = new Gson();
        Connection connection = null;
        PreparedStatement getAllStatement;

        ArrayList<Category> categories = new ArrayList<Category>();
        try {
            connection = DBConnection.createConnection();
            getAllStatement = connection.prepareStatement("SELECT * FROM categories");
            ResultSet rs = getAllStatement.executeQuery();
            while (rs.next()) {
                Category category = new Category();
                category.setCategoryID(rs.getInt("CategoryID"));
                category.setCategoryName(rs.getString("CategoryName"));
                categories.add(category);
            }

            HashMap hm = new HashMap();
            hm.put("Categories", categories);
            return gson.toJson(hm);

        } catch (Exception e) {
            e.printStackTrace();

            JSONObject response = new JSONObject();
            response.put("error", e.getLocalizedMessage());
            return response.toJSONString();
        }finally {
            if (connection != null) {
                connection.close();
            }
        }

    }


    @GET
    @Path("/addCategory")
    @Produces(MediaType.APPLICATION_JSON)
    public String addCategory(@QueryParam("categoryName") String categoryName) throws SQLException {
        Connection connection = null;
        Boolean added = false;
        String error = "";
        PreparedStatement insertCategory;
        int categoryID = -1;
        try {
            connection = DBConnection.createConnection();
            insertCategory = connection.prepareStatement(
                    "INSERT INTO categories(CategoryName)" +
                            "VALUES (?)", Statement.RETURN_GENERATED_KEYS);
            insertCategory.setString(1, categoryName);

            int records = insertCategory.executeUpdate();
            if (records > 0) {
                added = true;
                ResultSet keysSet = insertCategory.getGeneratedKeys();
                keysSet.next();
                categoryID = keysSet.getInt(1);
            } else {
                error = "Couldn't add category";
            }
        }catch (Exception e) {
            e.printStackTrace();
            error = e.getLocalizedMessage();
        }finally {
            if (connection != null) {
                connection.close();
            }
        }
        JSONObject response = new JSONObject();
        response.put("added", added);
        response.put("error", error);
        response.put("categoryID", categoryID);
        return response.toJSONString();
    }

    @GET
    @Path("/updateCategory")
    @Produces(MediaType.APPLICATION_JSON)
    public String updateCategory(@QueryParam("newCategoryName") String newCategoryName,
                                 @QueryParam("categoryID") int categoryID) throws SQLException {
        Connection connection = null;
        Boolean updated = false;
        String error = "";
        PreparedStatement updateCategory;
        try {
            connection = DBConnection.createConnection();
            updateCategory = connection.prepareStatement(
                    "UPDATE categories SET CategoryName=? WHERE CategoryID=?");
            updateCategory.setString(1, newCategoryName);
            updateCategory.setInt(2, categoryID);

            int records = updateCategory.executeUpdate();
            if (records > 0) {
                updated = true;
            } else {
                error = "Couldn't update category";
            }
        }catch (Exception e) {
            e.printStackTrace();
            error = e.getLocalizedMessage();
        }finally {
            if (connection != null) {
                connection.close();
            }
        }
        JSONObject response = new JSONObject();
        response.put("updated", updated);
        response.put("error", error);
        return response.toJSONString();
    }


    @GET
    @Path("/deleteCategory")
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteCategory(@QueryParam("categoryID") int categoryID) throws SQLException {
        Connection connection = null;
        Boolean deleted = false;
        String error = "";
        PreparedStatement deleteCategory;
        try {
            connection = DBConnection.createConnection();
            deleteCategory = connection.prepareStatement(
                    "DELETE FROM categories WHERE CategoryID=?");
            deleteCategory.setInt(1, categoryID);

            int records = deleteCategory.executeUpdate();
            if (records > 0) {
                deleted = true;
            } else {
                error = "Couldn't update category";
            }
        }catch (Exception e) {
            e.printStackTrace();
            error = e.getLocalizedMessage();
        }finally {
            if (connection != null) {
                connection.close();
            }
        }
        JSONObject response = new JSONObject();
        response.put("deleted", deleted);
        response.put("error", error);
        return response.toJSONString();
    }


    @GET
    @Path("/getCategoryByName")
    @Produces(MediaType.APPLICATION_JSON)
    public String getCategoryByName(@QueryParam("categoryName") String categoryName)
    {
        Connection connection = null;
        Boolean found = false;
        int categoryID = -1;
        String error = "";
        PreparedStatement selectCategory;
        try {
            connection = DBConnection.createConnection();
            selectCategory = connection.prepareStatement(
                    "SELECT CategoryID from categories where CategoryName LIKE ?");
            selectCategory.setString(1, categoryName);


            ResultSet rs = selectCategory.executeQuery();
            if(rs.next())
            {
                categoryID= rs.getInt("CategoryID");
                found = true;
            }
        }catch (Exception e) {
            e.printStackTrace();
            error = e.getLocalizedMessage();
        }finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        JSONObject response = new JSONObject();
        response.put("found", found);
        response.put("categoryID", categoryID);
        response.put("error", error);
        return response.toJSONString();
    }


}

class Category {
    int categoryID;

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    String categoryName;
}
