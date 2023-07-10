package ru.flykby;

import java.util.ArrayList;
import java.util.List;

import ru.flykby.entities.DataBuffer;
import ru.flykby.entities.DataChannel;
import ru.flykby.entities.DataPosting;
import ru.flykby.entities.DataUser;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataBase {
    private Connection connection;
    private Statement statement;

    public DataBase() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/java_autoposting_bot",
                "postgres",
                "170305"
            );
            statement = connection.createStatement();
            System.out.println("Database is connected!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Integer> getCountPost() {
        List<Integer> counts = new ArrayList<>();
        ResultSet resultSet;
        try {
            List<String> chanels = new ArrayList<>();
            resultSet = statement.executeQuery("SELECT namechanel FROM chanels");
            
            while (resultSet.next()) {
                chanels.add(resultSet.getString(1));
            }
            resultSet.close();
            
            for (String chanel : chanels) {
                String query = String.format("SELECT COUNT (*) FROM posts WHERE (chanel = '%s')", chanel);
                ResultSet res = statement.executeQuery(query);
                if (res.next()) {
                    counts.add(res.getInt(1));
                }
                res.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public boolean addPost(String chanel, DataBuffer dataBuffer) {
        try {
            String query = String.format(
                "INSERT INTO posts (chanel, message, photo_id, is_photo) VALUES ('%s', '%s', '%s', '%s')",
                chanel,
                dataBuffer.getMessage(),
                dataBuffer.getPhotoId(),
                dataBuffer.getIsPhoto()
            );
            int result = statement.executeUpdate(query);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<DataPosting> getPosts(String channel) {
        List<DataPosting> dataPostings = new ArrayList<>();
        try {
            String query = String.format("SELECT * FROM posts WHERE (chanel = '%s') ORDER BY id", channel);
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()){
                DataPosting dataPosting = new DataPosting(
                        channel,
                        resultSet.getString("photo_id"),
                        resultSet.getString("message"),
                        resultSet.getString("is_photo")
                );
                dataPosting.setId(resultSet.getInt("id"));
                dataPostings.add(dataPosting);
            }
            // for (DataPosting elem : dataPostings) {
            //     System.out.println(elem);
            // }
        } catch (SQLException e) {
            e.getStackTrace();
        }

        return dataPostings;
    }

    public boolean deletePost(int id) {
        try {
            String query = String.format("DELETE FROM posts WHERE (id = %d)", id);
            int res = statement.executeUpdate(query);
            return true;
        } catch (SQLException e) {
            e.getStackTrace();
            return false;
        }
    }

    public List<DataPosting> getDataPosting() {
        List<DataPosting> dataPostings = new ArrayList<>();
        try {
            List<DataChannel> chanels = getChanels();
            for (DataChannel channel : chanels) {
                String query = String.format("SELECT * FROM posts WHERE (chanel = '%s') ORDER BY id LIMIT 1", channel.getNamechannel());
                ResultSet resultSet = statement.executeQuery(query);
                int id = 0;
                if (resultSet.next()) {
                    id = resultSet.getInt("id");
                    dataPostings.add(new DataPosting(
                            channel.getNamechannel(),
                            resultSet.getString("photo_id"),
                            resultSet.getString("message"),
                            resultSet.getString("is_photo")));
                }
                resultSet.close();
                query = String.format("DELETE FROM posts WHERE (id = %d)", id);
                int res = statement.executeUpdate(query);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataPostings;
    }

    public boolean addChanel(String name, String namechanel) {
        try {
            String query = String.format("INSERT INTO chanels (name, namechanel) VALUES ('%s', '%s')", name, namechanel);
            int result = statement.executeUpdate(query);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteChanel(String namechanel) {
        try {
            String query = String.format("DELETE FROM chanel WHERE (namechanel = '%d')", namechanel);
            int result = statement.executeUpdate(query);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<DataChannel> getChanels() {
        List<DataChannel> chanels = new ArrayList<>();
        try {
            String query = String.format("SELECT name, namechanel FROM chanels");
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()){
                chanels.add(
                    new DataChannel(
                        resultSet.getString("name"),
                        resultSet.getString("namechanel")));
            }
            resultSet.close();
            return chanels;
        } catch (SQLException e) {
            e.printStackTrace();
            return chanels;
        }
    }

    public boolean addUser(String userId, String channelId, int date) {
        try {
            String query = String.format("INSERT INTO users (id, channel_id, date) VALUES ('%s', '%s', '%d')", userId, channelId, date);
            int result = statement.executeUpdate(query);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUser(String userId, String channelId) {
        try {
            String query = String.format("DELETE FROM users WHERE (id = %s AND channel_id = '%s')", userId, channelId);
            int result = statement.executeUpdate(query);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<DataUser> getUsers(int cooldown) {
        List<DataUser> users = new ArrayList<>();
        try {
            long unixTime = System.currentTimeMillis() / 1000L;
            String query = String.format("SELECT * FROM users WHERE (date < '%d')", unixTime - cooldown);
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()){
                users.add(
                    new DataUser(
                        resultSet.getLong("id"),
                        resultSet.getString("channel_id")));
            }
            resultSet.close();
            return users;
        } catch (SQLException e) {
            e.printStackTrace();
            return users;
        }
    }



    public void closeDataBase() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
