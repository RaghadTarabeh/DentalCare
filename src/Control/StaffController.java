package Control;

import Entity.Staff;
import Control.DatabaseConnection;
import java.sql.*;

public class StaffController {

    public static Staff getDentistById(int staffId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM Staff WHERE StaffID = ? AND RoleID = 1"; // RoleID 1 = Dentist
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, staffId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Debug: Print available columns
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                System.out.println("Available columns in Staff table:");
                for (int i = 1; i <= columnCount; i++) {
                    System.out.println("Column " + i + ": " + metaData.getColumnName(i));
                }

                // Based on your database structure and IDE suggestions, use this constructor:
                Staff dentist = new Staff(
                    rs.getInt("StaffID"),
                    rs.getString("FirstName"),
                    rs.getString("LastName"),
                    rs.getString("PhoneNumber"),
                    rs.getString("EmailAddress"),
                    rs.getInt("RoleID"),
                    rs.getInt("SpecializationID"),
                    rs.getString("Qualification"),
                    rs.getString("ScheduleDetails"),  // This column exists in your database
                    rs.getBoolean("IsClinicManager")  // This column exists in your database
                );

                rs.close();
                stmt.close();
                conn.close();

                System.out.println("StaffController - Found dentist: " + dentist.getFirstName() + " " + dentist.getLastName());
                return dentist;
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            System.err.println("StaffController - Error loading dentist: " + e.getMessage());
            e.printStackTrace();
        } 

        System.out.println("StaffController - Dentist not found with ID: " + staffId);
        return null;
    }
    
    
}