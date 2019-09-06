package clcondorcet.olympa.fr.olympaBot;

import java.io.*;
import java.sql.*;

public class SQL {
    private String url= "";
    ResultSet resultSet = null;
    Statement statement = null;
    String driver = "";
    String user = "";
    String database = "";
    String password = "";
    String port = "";
    String host = "";
    Connection c = null;
    
    public SQL(String Host, String db, String username, String password) {
        this.host = Host;
        this.database = db;
        this.user = username;
        this.password = password;
        url = "jdbc:mysql://" + host + "/" + database + "?user=" + user + "&password=" + password;
        driver = ("com.mysql.jdbc.Driver");
    }

    public SQL(String filePath) {
        url = "jdbc:sqlite:" + new File(filePath).getAbsolutePath();
        driver = ("org.sqlite.JDBC");
    }

    public Connection open() throws ClassNotFoundException, SQLException {
    	Class.forName(driver);
        this.c = DriverManager.getConnection(url);
        return c;
    }

    public boolean checkConnection() {
        if (this.c != null) {
            return true;
        }
        return false;
    }

    public Connection getConn() {
        return this.c;
    }

    public void closeConnection(Connection c) {
        c = null;
    }
    
    public ResultSet get(String syntax) throws SQLException {
        ResultSet res = null;
        res = this.getConn().createStatement(1003, 1007).executeQuery(syntax);
        return res;
    }
    
    public void set(String syntax) throws SQLException {
    	this.getConn().createStatement().executeUpdate(syntax);
    }
    
    public void setNoErrorLog(String syntax) {
        try{
        	this.getConn().createStatement().executeUpdate(syntax);
        }
        catch (SQLException e) {}
    }
    
    public boolean isTable(final String table) {
        Statement statement;
        try {
            statement = this.getConn().createStatement();
        }
        catch (SQLException e) {
            return false;
        }
        try {
            statement.executeQuery("SELECT * FROM " + table);
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }
}