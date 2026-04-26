package Control;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import Entity.Consts;

public class DatabaseConnection {
    
    static {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                "UCanAccess Driver not found on classpath: " + e.getMessage()
            );
        }
    }
    String url = "jdbc:ucanaccess://Database/ex1_database_2025_RT2.accdb";

    
    /** מחזיר חיבור למסד Access באמצעות UCanAccess */
    public static Connection getConnection() throws SQLException {
        try {
            System.out.println("DatabaseConnection - Attempting to connect using: " + Consts.CONN_STR);
            Connection conn = DriverManager.getConnection(Consts.CONN_STR);
            System.out.println("DatabaseConnection - Connection successful!");
            return conn;
        } catch (SQLException e) {
            System.err.println("DatabaseConnection - Connection failed: " + e.getMessage());
            throw e;
        }
    }
}